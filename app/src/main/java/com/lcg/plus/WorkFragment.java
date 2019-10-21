package com.lcg.plus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lcg.annotation.AutoField;

import java.util.Date;

/**
 * 课程页
 *
 * @author lei.chuguang Email:475825657@qq.com
 * @version 1.0
 * @since 2017/1/4 11:48
 */
public class WorkFragment extends Fragment {
    @AutoField
    boolean isMove;
    @AutoField
    Date ctime;
    @AutoField
    String cid;

    public static WorkFragment newInstance(boolean isMove, Date ctime, String cid) {
        WorkFragment fragment = new WorkFragment();
        fragment.setArguments(new WorkFragmentBundleBuilder()
                .setCid(cid)
                .setCtime(ctime)
                .setIsMove(isMove)
                .build());
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            isMove = arguments.getBoolean("isMove", false);
            ctime = (Date) arguments.getSerializable("ctime");
            cid = arguments.getString("cid");
        }
        TextView tv = new TextView(inflater.getContext());
        tv.setText(isMove + "----" + ctime + "-----" + cid);
        return tv;
    }
}
