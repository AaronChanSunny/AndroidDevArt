package com.aaron.androiddevart;

import java.io.Serializable;

/**
 * Created by Git on 2016/3/10.
 */
public class User implements Serializable {
    private static final long serialVersionUID = -505048669892056525L;

    private int mId;
    private String mName;
    private boolean mIsMale;

    public User(int id, String name, boolean isMale) {
        mId = id;
        mName = name;
        mIsMale = isMale;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public boolean isMale() {
        return mIsMale;
    }

    public void setIsMale(boolean isMale) {
        mIsMale = isMale;
    }
}
