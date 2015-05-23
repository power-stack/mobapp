package com.stackbase.mobapp;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.stackbase.mobapp.objects.Borrower;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;

import java.util.ArrayList;
import java.util.List;

public class CollectActivity extends FragmentActivity implements ActionBar.TabListener {
    List<Fragment> frags;
    FragmentIDCard idCard;
    FragmentOtherInfo otherInfo;
    ActionBar actionBar;
    ViewPager vp;
    private String TAG = CollectActivity.class.getSimpleName();

    FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        @Override
        public Fragment getItem(int position) {
            return frags.get(position);
        }

        @Override
        public int getCount() {
            return frags.size();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collect);

        initView();

        // ActionBar is initiated
        actionBar = getActionBar();

        // Declaration a ViewPager
        vp = (ViewPager) findViewById(R.id.collect);
        //vp.setOffscreenPageLimit(2);

        // Tell the ActionBar we want to use Tabs
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        final ActionBar.Tab idcardTab = actionBar.newTab().setText(getResources().getText(R.string.id_card));
        ActionBar.Tab otherTab = actionBar.newTab().setText(getResources().getText(R.string.other_info));

        // set the Tab listener. Now we can listen for clicks
        idcardTab.setTabListener(this);
        otherTab.setTabListener(this);

        // add the two tabs to the action bar
        actionBar.addTab(idcardTab);
        actionBar.addTab(otherTab);

        // Set a adapter for ViewPager
        vp.setAdapter(pagerAdapter);

        vp.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    validateID();
                    Helper.hideSoftKeyboard(CollectActivity.this);
                }
                actionBar.setSelectedNavigationItem(position);
            }
        });
    }

    private void initView() {
        frags = new ArrayList<Fragment>();

        // create the 2 fragments to display content
        idCard = new FragmentIDCard();
        otherInfo = new FragmentOtherInfo();

        frags.add(idCard);
        frags.add(otherInfo);

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        Log.d(TAG, "onTabReselected: " + tab.getPosition());
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        Log.d(TAG, "onTabSelected: " + tab.getPosition());
        vp.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        Log.d(TAG, "onTabUnselected: " + tab.getPosition());
    }

    @Override
    public void finish() {
        Log.d(TAG, "finish");
        if (idCard.isFromManage()) {
            idCard.validateIdCardInputs();
            Intent intent = new Intent();
            intent.putExtra(Constant.INTENT_KEY_BORROWER_OBJ, idCard.getBorrower());
            this.setResult(Activity.RESULT_OK, intent);
        }
        super.finish();
    }

    private void validateID() {
        if (!idCard.validateIdCardInputs()) {
            DialogInterface.OnClickListener alertListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    vp.setCurrentItem(0);
                    actionBar.selectTab(actionBar.getTabAt(0));
                }
            };
            Helper.showErrorMessage(CollectActivity.this, getString(R.string.err_title),
                    getString(R.string.err_id_needed),
                    null, alertListener);
        }

    }

    public boolean saveBorrowerInfo(Borrower borrower) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String rootDir = prefs.getString(Constant.KEY_STORAGE_DIR,
                Constant.DEFAULT_STORAGE_DIR);
        boolean result = Helper.saveBorrower(borrower, rootDir);
        if (result) {
            getIntent().putExtra(Constant.INTENT_KEY_ID_JSON_FILENAME, borrower.getJsonFile());
        }
        return result;
    }

}
