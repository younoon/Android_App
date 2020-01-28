package nhs75007.nagoya.ac.todo_list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import nhs75007.nagoya.ac.todo_list.R;

public class CustomAdapter extends ArrayAdapter<ToDoData> {
    private List<ToDoData>mCards;

    public CustomAdapter(Context context,int layoutResourceId,List<ToDoData> toDoData){
        super(context,layoutResourceId,toDoData);

        this.mCards = toDoData;
    }

    @Override
    public int getCount(){
        return mCards.size();
    }

    @NonNull
    @Override
    public ToDoData getItem(int position){
        return mCards.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent){
        final ViewHolder viewHolder;

        if (convertView != null){
            viewHolder = (ViewHolder) convertView.getTag();
        }else{
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.card_view,null);
            viewHolder = new ViewHolder();

            viewHolder.titleTextView = (TextView) convertView.findViewById(R.id.title_text_view);
            viewHolder.contentTextView = (TextView) convertView.findViewById(R.id.content_text_view);
            convertView.setTag(viewHolder);
        }

        ToDoData toDoData = mCards.get(position);

        viewHolder.titleTextView.setText(toDoData.getTitle());
        viewHolder.contentTextView.setText(toDoData.getContent());

        return convertView;
    }

    public ToDoData getToDoDataKey(String key){
        for (ToDoData toDoData : mCards){
            if (toDoData.getFirebaseKey().equals(key)){
                return toDoData;
            }
        }

        return null;
    }

    static class ViewHolder{
        TextView titleTextView;
        TextView contentTextView;
    }


}
