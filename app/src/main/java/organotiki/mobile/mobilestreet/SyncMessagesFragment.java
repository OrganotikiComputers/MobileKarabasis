package organotiki.mobile.mobilestreet;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import io.realm.Realm;
import organotiki.mobile.mobilestreet.objects.GlobalVar;

/**
 * Created by Thanasis on 12/9/2016.
 */
public class SyncMessagesFragment extends DialogFragment {

    int ParentButton;
    Button close;
    ListView listView;
    TextView title;
    Realm realm;
    GlobalVar gVar;
    ArrayList<String> messages;
    MyListAdapter myListAdapter;
    View view;
    VolleyRequests request;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            view = inflater.inflate(R.layout.fragment_sync_dialog, null);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();
            messages = new ArrayList<>();
            String message0 = "Ο συγχρονισμός έχει ξεκινήσει, παρακαλώ περιμένετε.";
            messages.add(message0);
            title = view.findViewById(R.id.textView_title);
            title.setText(R.string.app_name);
            listView = view.findViewById(R.id.listView_sync_messages);
            myListAdapter = new MyListAdapter(getActivity(), messages);
            listView.setAdapter(myListAdapter);
//            listView.setStackFromBottom(true);
            listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            close = view.findViewById(R.id.button_close);
            close.setTransformationMethod(null);
            close.setText(getString(R.string.close));
            close.setEnabled(false);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            if (ParentButton==0){
                request = new VolleyRequests();
                request.sendAuthenticationRequest(getActivity());
            }else if (ParentButton==1){
                request = new VolleyRequests();
                request.sendSyncEmail(getActivity());
            }
            else if (ParentButton==2){
                request = new VolleyRequests();
                request.syncEvents(getActivity(),gVar.getMyUser().getUsername(),"0");
            }

            setCancelable(false);
            return view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    public void setParentButton(int parentButton) {
        ParentButton = parentButton;
    }

    public void addNewMessage(String message){
        try {
//            myListAdapter.add(message);
            Log.d("asdfg", "message="+ String.valueOf(message));
            Log.d("asdfg", "messages="+String.valueOf(messages));
            /*if (messages==null){
                messages = new ArrayList<>();
            }*/
            messages.add( message);
           /* if (myListAdapter==null){
                myListAdapter = new MyListAdapter(getActivity(), messages);
            }*/
            myListAdapter.notifyDataSetChanged();
//            scrollMyListViewToBottom();
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void enableButton(){
        try {
            /*if (close==null){
                close = (Button) view.findViewById(R.id.button_close);
            }*/
            setCancelable(true);
            close.setEnabled(true);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public class  MyListAdapter extends ArrayAdapter<String>{

        Realm realm;
        GlobalVar gVar;

        private ArrayList<String> messages;

        private class ViewHolder{
            TextView Message;
            int ref;
        }

        public MyListAdapter(Context context, ArrayList<String> messages) {
            super(context, 0, messages);
            this.messages = messages;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            try {
                // Get the data item for this position
                final String message = messages.get(position);
                realm = Realm.getDefaultInstance();
                gVar = realm.where(GlobalVar.class).findFirst();

                final ViewHolder viewHolder;
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(R.layout.listview_sync_messages, parent, false);
                    viewHolder.Message = convertView.findViewById(R.id.textView_message);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                viewHolder.ref = position;
                viewHolder.Message.setText(message);

                } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
            return convertView;
        }
    }
}
