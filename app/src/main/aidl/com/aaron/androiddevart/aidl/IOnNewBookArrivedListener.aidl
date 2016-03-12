// IOnNewBookArrivedListener.aidl
package com.aaron.androiddevart.aidl;

// Declare any non-default types here with import statements
import com.aaron.androiddevart.aidl.Book;

interface IOnNewBookArrivedListener {
    void onNewBookArrived(in Book newBook);
}
