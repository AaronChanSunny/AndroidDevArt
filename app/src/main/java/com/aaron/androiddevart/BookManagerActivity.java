package com.aaron.androiddevart;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.aaron.androiddevart.aidl.Book;
import com.aaron.androiddevart.aidl.IBookManager;
import com.aaron.androiddevart.aidl.IOnNewBookArrivedListener;

import java.util.ArrayList;
import java.util.List;

public class BookManagerActivity extends AppCompatActivity {

    private static final String TAG = BookManagerActivity.class.getSimpleName();
    private static final int MESSAGE_NEW_BOOK_ARRIVED = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_NEW_BOOK_ARRIVED:
                    mBooks.add(msg.obj);
                    mAdapter.notifyDataSetChanged();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
    private IBookManager mRemoteBookManager;
    private IOnNewBookArrivedListener mIOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook).sendToTarget();
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRemoteBookManager = IBookManager.Stub.asInterface(service);
            try {
                List<Book> list = mRemoteBookManager.getBookList();
                Log.d(TAG, "get book list, list type:" + list.getClass().getCanonicalName());
                Log.d(TAG, "get book list:" + list.toString());

                mRemoteBookManager.addBook(new Book(3, "Android 开发艺术探索"));
                List<Book> newList = mRemoteBookManager.getBookList();
                Log.d(TAG, "get book new list, list type:" + newList.getClass().getCanonicalName());
                Log.d(TAG, "get book new list:" + newList.toString());

                mRemoteBookManager.registerListener(mIOnNewBookArrivedListener);

                mBooks.clear();
                mBooks.addAll(newList);
                mAdapter.notifyDataSetChanged();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteBookManager = null;
            Log.e(TAG, "binder died.");
        }
    };
    private List mBooks = new ArrayList();
    private ArrayAdapter<Book> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_manager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);

        ListView listView = (ListView) findViewById(R.id.books);
        mAdapter = new ArrayAdapter<>(this, android.R.layout
                .simple_list_item_1, android.R.id.text1, mBooks);
        listView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        if (mRemoteBookManager != null && mRemoteBookManager.asBinder().isBinderAlive()) {
            try {
                mRemoteBookManager.unregisterListener(mIOnNewBookArrivedListener);
                Log.d(TAG, "unregister listener: " + mIOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        super.onDestroy();
    }
}
