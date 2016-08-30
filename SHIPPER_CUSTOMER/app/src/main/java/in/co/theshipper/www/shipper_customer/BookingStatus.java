package in.co.theshipper.www.shipper_customer;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookingStatus extends Fragment implements ActionBar.TabListener{
    View view;
    ViewPager view_pager;
    ActionBar actionBar;
    public BookingStatus() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (container == null) {
            return null;
        } else {
            view = inflater.inflate(R.layout.fragment_booking_status, container, false);
            return view;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null) {
            view_pager = (ViewPager) view.findViewById(R.id.view_pager);
            view_pager.setAdapter(new MyAdapter(FullActivity.fragmentManager));
            view_pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Fn.logD("onPageScrolled","scrolled at position:"+position+"from: "+positionOffset+"with no of pixel"+positionOffsetPixels);
                }

                @Override
                public void onPageSelected(int position) {
                    actionBar.setSelectedNavigationItem(position);
//                Fn.logD("onPageSelected"," at position:"+position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
//                Fn.logD("onPageScrollStateChanged","state:"+state);
                }
            });

            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.removeAllTabs();
            if (actionBar.getTabCount() == 0) {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                ActionBar.Tab tab1 = actionBar.newTab();
                tab1.setText(R.string.title_current_booking_fragment);
                ActionBar.Tab tab2 = actionBar.newTab();
                tab2.setText(R.string.title_future_booking_fragment);
                ActionBar.Tab tab3 = actionBar.newTab();
                tab3.setText(R.string.title_finished_booking_fragment);
                tab1.setTabListener(this);
                tab2.setTabListener(this);
                tab3.setTabListener(this);
                actionBar.addTab(tab1);
                actionBar.addTab(tab2);
                actionBar.addTab(tab3);
            }
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
//      Fn.logD("onTabSelected","position:"+tab.getPosition()+"name"+tab.getText());
        view_pager.setCurrentItem(tab.getPosition());
    }
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
//        Fn.logD("onTabUnselected","position:"+tab.getPosition()+"name"+tab.getText());
    }
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
//        Fn.logD("onTabReselected", "position:" + tab.getPosition() + "name" + tab.getText());
    }
    class MyAdapter extends FragmentStatePagerAdapter {
        public MyAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
//            if (fragmentManager.getFragments() != null) {
//                fragmentManager.getFragments().clear();
//            }
        }
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch(position){
                case 0:
                fragment = new CurrentBooking();
                    break;
                case 1:
                    fragment = new FutureBooking();
                    break;
                case 2:
                    fragment = new FinishedBooking();
                    break;
            }
            return fragment;
        }
        @Override
        public int getCount() {
            int count = 3;
            return count;
        }
//        @Override
//        public CharSequence getPageTitle(int position) {
//
//            String title = "";
//            switch(position){
//                case 0:
//                    title =  "CurrentBookingDetails";
//                    break;
//                case 1:
//                    title =  "FutureBooking";
//                    break;
//                case 2:
//                    title =  "FinishedBooking";
//                    break;
//            }
//            return title;
//        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fn.logE("BOOKING_STATUS_FRAGMENT_LIFECYCLE", "onAttach Called");
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Fn.logE("BOOKING_STATUS_FRAGMENT_LIFECYCLE", "onSaveInstanceState Called");
    }
    @Override
    public void onStart() {
        super.onStart();
        Fn.logE("BOOKING_STATUS_FRAGMENT_LIFECYCLE", "onStart Called");
    }
    @Override
    public void onResume() {
        super.onResume();
        Fn.logE("BOOKING_STATUS_FRAGMENT_LIFECYCLE", "onResume Called");
    }
    @Override
    public void onPause() {
        super.onPause();
        Fn.logE("BOOKING_STATUS_FRAGMENT_LIFECYCLE", "onPause Called");
    }
    @Override
    public void onStop() {
        super.onStop();
        Fn.logE("BOOKING_STATUS_FRAGMENT_LIFECYCLE", "onStop Called");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
  //      actionBar.removeAllTabs();
//        actionBar.hide();
        Fn.logE("BOOKING_STATUS_FRAGMENT_LIFECYCLE", "onDestroy Called");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        Fn.logE("BOOKING_STATUS_FRAGMENT_LIFECYCLE", "onDestroyView Called");
        List<Fragment> fragments = FullActivity.fragmentManager.getFragments();
        Fn.logD("Fragments List",String.valueOf(fragments));
        if (fragments != null) {
            FragmentTransaction ft = FullActivity.fragmentManager.beginTransaction();
            for (Fragment f : fragments) {
                //You can perform additional check to remove some (not all) fragments:
                if ((f instanceof CurrentBooking)||(f instanceof FutureBooking)||(f instanceof FinishedBooking)) {
                    ft.remove(f);
                    Fn.logE("BOOKING_STATUS_FRAGMENT_LIFECYCLE", "All fragments removed");
                }
            }
            ft.commitAllowingStateLoss();
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        Fn.logE("BOOKING_STATUS_FRAGMENT_LIFECYCLE", "onDetach Called");

    }
}
