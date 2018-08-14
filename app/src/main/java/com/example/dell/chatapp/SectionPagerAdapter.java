package com.example.dell.chatapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.dell.chatapp.Frgments.ChatFragment;
import com.example.dell.chatapp.Frgments.FriendsFragment;
import com.example.dell.chatapp.Frgments.RequestFragment;

/**
 * Created by dell on 8/3/2018.
 */

public class SectionPagerAdapter extends FragmentPagerAdapter {

    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
    switch (position){
        case 0:
            RequestFragment requestFragment=new RequestFragment();
            return  requestFragment;
        case 1:
            ChatFragment chatFragment=new ChatFragment();
            return chatFragment;
        case 2:
            FriendsFragment friendsFragment=new FriendsFragment();
            return friendsFragment;
        default:
            return null;
    }

    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
         super.getPageTitle(position);
        switch (position) {
            case 0:
                return "REQUESTS";
            case 1:
                return  "CHATS";
            case 2:
                return  "FRIENDS";
            default:
                return null;
        }
    }
}
