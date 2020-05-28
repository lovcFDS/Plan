package com.example.plan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Build;
import android.os.Bundle;
import android.transition.Explode;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tvPlanList,tvPlanTime,tvAbout;

    private PlanListFragment planListFragment;
    private PlanTimeFragment planTimeFragment;
    private AboutFragment aboutFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_main);

        tvPlanList = (TextView)findViewById(R.id.main_plan_list);
        tvPlanTime = (TextView)findViewById(R.id.main_plan_time);
        tvAbout = (TextView)findViewById(R.id.main_about);

        planListFragment = new PlanListFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.main_container, planListFragment).commit();

        tvPlanList.setSelected(true);
        tvPlanTime.setOnClickListener(tabClickListener);
        tvPlanList.setOnClickListener(tabClickListener);
        tvAbout.setOnClickListener(tabClickListener);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH)
        {
            Explode explode = new Explode();
            getWindow().setEnterTransition(explode);
            getWindow().setExitTransition(explode);
        }

    }
    private View.OnClickListener tabClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            changeFragment(v.getId());
            changeSelect(v.getId());
        }
    };

    private void changeFragment(int resId)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction);
        switch(resId)
        {
            case R.id.main_plan_list:
                if(planListFragment == null)
                {
                    planListFragment = new PlanListFragment();
                    transaction.add(R.id.main_container, planListFragment);
                }
                else
                {
                    transaction.show(planListFragment);
                }
                break;
            case R.id.main_plan_time:
                if(planTimeFragment == null)
                {
                    planTimeFragment = new PlanTimeFragment();
                    transaction.add(R.id.main_container, planTimeFragment);
                }
                else
                {
                    transaction.show(planTimeFragment);
                }
                break;
            case R.id.main_about:
                if(aboutFragment == null)
                {
                    aboutFragment = new AboutFragment();
                    transaction.add(R.id.main_container, aboutFragment);
                }
                else
                {
                    transaction.show(aboutFragment);
                }
                break;
        }
        transaction.commit();
    }

    //隐藏所有的Fragment
    private void hideFragments(FragmentTransaction transaction)
    {
        if(planTimeFragment != null)
        {
            transaction.hide(planTimeFragment);
        }
        if(planListFragment != null)
        {
            transaction.hide(planListFragment);
        }
        if(aboutFragment != null)
        {
            transaction.hide(aboutFragment);
        }
    }

    //切换底部tap选择项
    private void changeSelect(int resId)
    {
        tvPlanList.setSelected(false);
        tvAbout.setSelected(false);
        tvPlanTime.setSelected(false);

        switch (resId)
        {
            case R.id.main_plan_list:
                tvPlanList.setSelected(true);
                break;
            case R.id.main_plan_time:
                tvPlanTime.setSelected(true);
                break;
            case R.id.main_about:
                tvAbout.setSelected(true);
                break;
        }
    }

}
