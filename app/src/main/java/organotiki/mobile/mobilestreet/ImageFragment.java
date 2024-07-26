package organotiki.mobile.mobilestreet;

import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by Thanasis on 25/5/2016.
 */
public class ImageFragment extends DialogFragment {

    String code;
    ImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            View view = inflater.inflate(R.layout.fragment_image,null);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            imageView = (ImageView) view.findViewById(R.id.imageView_item);

            try {
                //Picasso.with(getActivity().getApplicationContext()).load("http://192.168.0.34:8080/GetImage/"+cusCode).placeholder(getActivity().getResources().getDrawable(R.drawable.logo)).into(imageView);
                String filepath = Environment.getExternalStorageDirectory().getPath() + "/Pictures/" + code+".jpg";
                File file = new File(filepath);
                if(file.exists()){
                    Log.d("asdfg", filepath);
                    imageView.setImageURI(Uri.parse(filepath));
                }else{
                    imageView.setImageResource(R.drawable.logoo);
                    Log.d("asdfg", filepath+" does not exist");
                }
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
            return view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    public void setImageCode(String imageCode)
    {
        //imageUrl = url;
        code = imageCode;
        Log.d("asdfg", "hi");
    }
}