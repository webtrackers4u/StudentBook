package net.reminderbook.studentbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.Toast;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

public class AccountFragment extends Fragment {
    private Global os;
    private ImageView prfile_pic;
    private TextView member_name, member_email, member_board, member_class, member_type, member_address, member_mobile, title_member_name;
    private LinearLayout logout_button;

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        os = new Global(getContext());
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        prfile_pic = view.findViewById(R.id.profile_pic);
        title_member_name = view.findViewById(R.id.title_member_name);
        member_name = view.findViewById(R.id.member_name);
        member_email = view.findViewById(R.id.member_email);
        member_board = view.findViewById(R.id.member_board);
        member_class = view.findViewById(R.id.member_class);
        member_type = view.findViewById(R.id.type);
        member_address = view.findViewById(R.id.member_address);
        member_mobile = view.findViewById(R.id.member_mobile);
        logout_button = view.findViewById(R.id.logout_button);

        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                try {
                    ((MainActivity)getActivity()).logout();
                } catch (Exception e){
                    Toast.makeText(getContext(),"Error while logout", Toast.LENGTH_LONG).show();
                }

            }
        });
        getMemberDetails();
        return view;
    }


    private void getMemberDetails(){
        try {
            JSONObject data = new JSONObject(os.get_member_details());
            os.setImage(prfile_pic,data.getString("photo"),true);
            os.setText(title_member_name,data.getString("name"));
            os.setText(member_name,data.getString("name"));
            os.setText(member_email, data.getString("email"));
            os.setText(member_board, data.getString("boardName"));
            os.setText(member_class, data.getString("className"));
            os.setText(member_type, data.getString("type"));
            os.setText(member_address, data.getString("address"));
            os.setText(member_mobile, data.getString("mobile"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}