package com.aaron.androiddevart;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aaron.androiddevart.aidl.Book;
import com.aaron.androiddevart.aidl.IBookManager;
import com.aaron.androiddevart.aidl.IOnNewBookArrivedListener;

import org.xmlpull.v1.XmlSerializer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Git on 2016/3/12.
 */
public class BookManagerService extends Service {
    private static final String TAG = BookManagerService.class.getSimpleName();

    private AtomicBoolean mIsDestroyed = new AtomicBoolean(false);
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<IOnNewBookArrivedListener> mListeners = new
            CopyOnWriteArrayList<>();
    private Binder mBinder = new IBookManager.Stub() {
        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
            } else {
                Log.d(TAG, "already registered.");
            }

            Log.d(TAG, "registerd listener size: " + mListeners.size());
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            if (mListeners.contains(listener)) {
                mListeners.remove(listener);
                Log.d(TAG, "unregister listener succees.");
            } else {
                Log.d(TAG, "not found, can not unregister.");
            }

            Log.d(TAG, "unregisterd listener. current size: " + mListeners.size());
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1, "Android"));
        mBookList.add(new Book(2, "IOS"));

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mIsDestroyed.get()) {
                    SystemClock.sleep(5000);
                    int bookId = mBookList.size() + 1;
                    Book newBook = new Book(bookId, "new book#" + bookId);

                    try {
                        onNewBookArrived(newBook);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void onNewBookArrived(Book newBook) throws RemoteException {
        mBookList.add(newBook);
        Log.d(TAG, "new book arrived, notify listeners: " + mListeners.size());

        for (IOnNewBookArrivedListener listener : mListeners) {
            Log.d(TAG, "new book arrived, notify listener: " + listener);
            listener.onNewBookArrived(newBook);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
