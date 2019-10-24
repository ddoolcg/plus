package com.lcg.plus;

import com.lcg.annotation.AutoField;
import com.lcg.mylibrary.BaseActivity;
import com.lcg.mylibrary.BaseObservableMe;

/**
 * @author lei.chuguang Email:475825657@qq.com
 * @version 1.0
 * @since 2019/10/23 18:24
 */
public class Main extends BaseObservableMe {
    @AutoField
    int i = 0;

    {
        System.out.println("------2--------");
    }

    Main(BaseActivity activity) {
        super(activity);
    }

    public Main(BaseActivity activity, int b) {
        this(activity);
    }

    {
        System.out.println("------4--------");
    }
}
