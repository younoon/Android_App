package nhs75007.nagoya.ac.todo_list;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ToDoActivity extends AppCompatActivity implements ListView.OnItemLongClickListener {
    public FirebaseUser user;
    public String uid;

    public FirebaseAuth mAuth;

    public FirebaseDatabase database;
    public DatabaseReference reference;

    public CustomAdapter mCustomAdapter;
    public ListView mListView;


    //x軸最低スワイプ距離
    private static final int SWIPE_MIN_DISTANCE = 50;

    //X軸最低すワイプスピード
    private static final int SWIPE_THRESSHOLD_VELOCITY = 200;

    //y軸の移動距離　これ以上なら判定しない
    private static final int SWIPE_MAX_OFFPATH = 250;

    //タッチイベントを処理するためのインターフェース
    private GestureDetector mGestureDetector;



    private TextView textView1;
    private TextView textView2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);



        //スワイプイベント用
        mGestureDetector = new GestureDetector(this,mOnGestureListener);
        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);



        //ログイン情報を取得
        user = FirebaseAuth.getInstance().getCurrentUser();

        //user id = Uid を取得する
        uid = user.getUid();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users").child(uid);

        mListView = (ListView)findViewById(R.id.list_view);

        //CustomAdapterをセット
        mCustomAdapter = new CustomAdapter(getApplicationContext(),R.layout.card_view,new ArrayList<ToDoData>());
        mListView.setAdapter(mCustomAdapter);

        //LongListenerを設定
        mListView.setOnItemLongClickListener(this);

        //firebaseと同期するリスナー
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //アイテムのリストを取得するか、アイテムのリストへの追加しないかをリッスンする
                ToDoData toDoData = dataSnapshot.getValue(ToDoData.class);
                mCustomAdapter.add(toDoData);
                mCustomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //リスト内のアイテムに対する変更がないかリッスン
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //リストから削除されるアイテムがないかリッスンする
                Log.d("ToDoActivity","onChildRemoved:" + dataSnapshot.getKey());
                ToDoData result = dataSnapshot.getValue(ToDoData.class);
                if (result == null) return;

                ToDoData item = mCustomAdapter.getToDoDataKey(result.getFirebaseKey());

                mCustomAdapter.remove(item);
                mCustomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //並べ替えリストの項目順に変更がないかリッスン
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //ログなどを記録するなどError字の処理を記載する
            }
        });
    }

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //Inflate the menu; this adds to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    public void addButton(View v){
        Intent intent = new Intent(this,AddActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent,View view,final int position,long id){
        final  ToDoData toDoData = mCustomAdapter.getItem(position);
        uid = user.getUid();

        new AlertDialog.Builder(this).setTitle("Done?").setMessage("この項目を完了しましたか").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //OK button pressed
                reference.child(toDoData.getFirebaseKey()).removeValue();
                mCustomAdapter.remove(toDoData);
            }
        }).setNegativeButton("No",null).show();
        return false;
    }

    public void logout(View v){
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        Intent intent = new Intent(ToDoActivity.this,LoginActivity.class);
        intent.putExtra("check",true);
        startActivity(intent);
        finish();
    }

    //タッチイベント
    @Override
    public boolean onTouchEvent(MotionEvent event){

        return mGestureDetector.onTouchEvent(event);
    }

    //タッチイベントのリスナー
    private final GestureDetector.SimpleOnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener(){
        //フリックイベント
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocity){

            try{
                //移動距離・スピードを出力
                float distance_x = Math.abs((event1.getX() - event2.getX()));
                float velocity_x = Math.abs(velocityX);
                textView1.setText(("横の移動距離" + distance_x + "横の移動スピード" + velocity_x));

                //Y軸の移動距離が大きすぎる場合
                if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFFPATH){
                    textView2.setText("盾の移動距離が大きすぎ");
                }

                //開始一から終了位置の移動距離が指定値より大きい
                //X軸の移動速度が指定より大きい
                else if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESSHOLD_VELOCITY){
                    textView2.setText("右から左");
                }

                //終了位置から開始位置の移動距離が指定より大きい
                //X軸の移動速度が指定より大きい
                else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESSHOLD_VELOCITY){
                    textView2.setText("左から右");
                }

            } catch (Exception e){
                //TODO
            }

            return false;
        }
    };
}
