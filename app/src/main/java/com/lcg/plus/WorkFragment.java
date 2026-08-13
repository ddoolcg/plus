package com.lcg.plus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
        fragment.setArguments(new BundleWorkFragmentBuilder()
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
        TextView tv = new TextView(inflater.getContext());
        tv.setText(isMove + "----" + ctime + "-----" + cid);
        return tv;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
