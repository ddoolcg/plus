package com.lcg.plugin


import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

/**
 * ASM tree visitor that implements the former Transform API instrumentation.
 *
 * @author Lei Chuguang
 * @date 2026-08-12
 */
class AutoFieldClassVisitor extends ClassNode {
    private static final String AUTO_FIELD_DESC = 'Lcom/lcg/annotation/AutoField;'
    private static final String BUNDLE_DESC = 'Landroid/os/Bundle;'
    private static final String BUNDLE_METHOD_DESC = "(${BUNDLE_DESC})V"

    private final ClassVisitor downstream
    private final Set<String> superClasses
    private String originalName
    private String originalSuperName

    AutoFieldClassVisitor(ClassVisitor downstream, Collection<String> superClasses = []) {
        super(Opcodes.ASM9)
        this.downstream = downstream
        this.superClasses = (superClasses ?: []).collect { it.replace('.', '/') } as Set<String>
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        originalName = name
        originalSuperName = superName
        super.visit(version, access, name, signature, superName, interfaces)
    }

    @Override
    void visitEnd() {
        transformClass()
        accept(downstream)
    }

    private void transformClass() {
        if ((access & (Opcodes.ACC_INTERFACE | Opcodes.ACC_ANNOTATION | Opcodes.ACC_ENUM)) != 0) return

        boolean hasAutoFields = fields.any { FieldNode field ->
            hasAnnotation(field.visibleAnnotations) || hasAnnotation(field.invisibleAnnotations)
        }
        Set<String> hierarchy = new HashSet<>(superClasses)
        if (originalSuperName != null) hierarchy.add(originalSuperName)
        println("Visiting class $name with super class ${hierarchy.toListString()}")
        if (hierarchy.contains('android/app/Activity')) {
            transformActivity(hasAutoFields)
        } else if (hierarchy.any { it in ['android/app/Fragment', 'androidx/fragment/app/Fragment'] }) {
            if (hasAutoFields) transformFragment()
        } else if (hierarchy.contains('com/lcg/mylibrary/BaseObservableMe') && hasAutoFields) {
            transformObservable()
        }
    }

    private static boolean hasAnnotation(List<AnnotationNode> annotations) {
        (annotations ?: []).any { it.desc == AUTO_FIELD_DESC }
    }

    private MethodNode findMethod(String name, String desc) {
        methods.find { MethodNode method -> method.name == name && method.desc == desc }
    }

    private String extrasOwner() {
        originalName + Constant.GENERATED_FILE_SUFFIX
    }

    private String extrasMethodDesc() {
        "(L${originalName};${BUNDLE_DESC})V"
    }

    private String initMethodDesc() {
        "(L${originalName};)V"
    }

    private void transformActivity(boolean hasAutoFields) {
        MethodNode onCreate = findMethod('onCreate', BUNDLE_METHOD_DESC)
        if (onCreate == null) {
            onCreate = new MethodNode(Opcodes.ASM9, Opcodes.ACC_PROTECTED, 'onCreate', BUNDLE_METHOD_DESC, null, null)
            addActivityRestore(onCreate.instructions, hasAutoFields)
            onCreate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
            onCreate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1))
            onCreate.instructions.add(new MethodInsnNode(
                    Opcodes.INVOKESPECIAL, originalSuperName, 'onCreate', BUNDLE_METHOD_DESC, false))
            onCreate.instructions.add(new InsnNode(Opcodes.RETURN))
            methods.add(onCreate)
        } else {
            InsnList prefix = new InsnList()
            addActivityRestore(prefix, hasAutoFields)
            onCreate.instructions.insert(prefix)
        }

        MethodNode onSave = findMethod('onSaveInstanceState', BUNDLE_METHOD_DESC)
        if (onSave == null) {
            onSave = new MethodNode(
                    Opcodes.ASM9, Opcodes.ACC_PROTECTED, 'onSaveInstanceState', BUNDLE_METHOD_DESC, null, null)
            addActivitySave(onSave.instructions, hasAutoFields)
            onSave.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
            onSave.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1))
            onSave.instructions.add(new MethodInsnNode(
                    Opcodes.INVOKESPECIAL, originalSuperName, 'onSaveInstanceState', BUNDLE_METHOD_DESC, false))
            onSave.instructions.add(new InsnNode(Opcodes.RETURN))
            methods.add(onSave)
        } else {
            InsnList prefix = new InsnList()
            addActivitySave(prefix, hasAutoFields)
            onSave.instructions.insert(prefix)
        }
    }

    private void addActivityRestore(InsnList instructions, boolean hasAutoFields) {
        LabelNode savedStateIsNull = new LabelNode()
        LabelNode complete = new LabelNode()
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1))
        instructions.add(new JumpInsnNode(Opcodes.IFNULL, savedStateIsNull))
        if (hasAutoFields) {
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 1))
            instructions.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC, extrasOwner(), 'onRestoreInstanceState', extrasMethodDesc(), false))
        } else {
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
            instructions.add(new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL, originalName, 'getIntent', '()Landroid/content/Intent;', false))
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 1))
            instructions.add(new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL, 'android/content/Intent', 'putExtras',
                    '(Landroid/os/Bundle;)Landroid/content/Intent;', false))
            instructions.add(new InsnNode(Opcodes.POP))
        }
        instructions.add(new JumpInsnNode(Opcodes.GOTO, complete))
        instructions.add(savedStateIsNull)
        if (hasAutoFields) {
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
            instructions.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC, extrasOwner(), 'onInitState', initMethodDesc(), false))
        }
        instructions.add(complete)
    }

    private void addActivitySave(InsnList instructions, boolean hasAutoFields) {
        if (hasAutoFields) {
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 1))
            instructions.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC, extrasOwner(), 'onSaveInstanceState', extrasMethodDesc(), false))
            return
        }

        LabelNode complete = new LabelNode()
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, originalName, 'getIntent', '()Landroid/content/Intent;', false))
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, 'android/content/Intent', 'getExtras', '()Landroid/os/Bundle;', false))
        instructions.add(new JumpInsnNode(Opcodes.IFNULL, complete))
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1))
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, originalName, 'getIntent', '()Landroid/content/Intent;', false))
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, 'android/content/Intent', 'getExtras', '()Landroid/os/Bundle;', false))
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, 'android/os/Bundle', 'putAll', '(Landroid/os/Bundle;)V', false))
        instructions.add(complete)
    }

    private void transformFragment() {
        MethodNode onSave = findMethod('onSaveInstanceState', BUNDLE_METHOD_DESC)
        if (onSave == null) {
            onSave = new MethodNode(Opcodes.ASM9, Opcodes.ACC_PUBLIC, 'onSaveInstanceState', BUNDLE_METHOD_DESC, null, null)
            addFragmentSave(onSave.instructions)
            onSave.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
            onSave.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1))
            onSave.instructions.add(new MethodInsnNode(
                    Opcodes.INVOKESPECIAL, originalSuperName, 'onSaveInstanceState', BUNDLE_METHOD_DESC, false))
            onSave.instructions.add(new InsnNode(Opcodes.RETURN))
            methods.add(onSave)
        } else {
            InsnList prefix = new InsnList()
            addFragmentSave(prefix)
            onSave.instructions.insert(prefix)
        }

        String onCreateViewDesc = '(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;'
        MethodNode onCreateView = findMethod('onCreateView', onCreateViewDesc)
        if (onCreateView == null) {
            onCreateView = new MethodNode(Opcodes.ASM9, Opcodes.ACC_PUBLIC, 'onCreateView', onCreateViewDesc, null, null)
            addFragmentRestore(onCreateView.instructions)
            onCreateView.instructions.add(new InsnNode(Opcodes.ACONST_NULL))
            onCreateView.instructions.add(new InsnNode(Opcodes.ARETURN))
            methods.add(onCreateView)
        } else {
            InsnList prefix = new InsnList()
            addFragmentRestore(prefix)
            onCreateView.instructions.insert(prefix)
        }
    }

    private void addFragmentSave(InsnList instructions) {
        LabelNode noArguments = new LabelNode()
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, originalName, 'getArguments', '()Landroid/os/Bundle;', false))
        instructions.add(new JumpInsnNode(Opcodes.IFNULL, noArguments))
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1))
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, originalName, 'getArguments', '()Landroid/os/Bundle;', false))
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, 'android/os/Bundle', 'putAll', '(Landroid/os/Bundle;)V', false))
        instructions.add(noArguments)
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1))
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC, extrasOwner(), 'onSaveInstanceState', extrasMethodDesc(), false))
    }

    private void addFragmentRestore(InsnList instructions) {
        LabelNode useArguments = new LabelNode()
        LabelNode complete = new LabelNode()
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 3))
        instructions.add(new JumpInsnNode(Opcodes.IFNULL, useArguments))
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 3))
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC, extrasOwner(), 'onRestoreInstanceState', extrasMethodDesc(), false))
        instructions.add(new JumpInsnNode(Opcodes.GOTO, complete))
        instructions.add(useArguments)
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, originalName, 'getArguments', '()Landroid/os/Bundle;', false))
        instructions.add(new JumpInsnNode(Opcodes.IFNULL, complete))
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, originalName, 'getArguments', '()Landroid/os/Bundle;', false))
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC, extrasOwner(), 'onRestoreInstanceState', extrasMethodDesc(), false))
        instructions.add(complete)
    }

    private void transformObservable() {
        methods.findAll { it.name == '<init>' }.each { MethodNode constructor ->
            AbstractInsnNode instruction = constructor.instructions.first
            while (instruction != null) {
                if (instruction instanceof MethodInsnNode &&
                        instruction.opcode == Opcodes.INVOKESPECIAL &&
                        instruction.name == '<init>') {
                    if (instruction.owner == originalSuperName) {
                        InsnList initialization = new InsnList()
                        initialization.add(new VarInsnNode(Opcodes.ALOAD, 0))
                        initialization.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC, extrasOwner(), 'onInitState', initMethodDesc(), false))
                        constructor.instructions.insert(instruction, initialization)
                        break
                    }
                    if (instruction.owner == originalName) break
                }
                instruction = instruction.next
            }
        }
    }
}
