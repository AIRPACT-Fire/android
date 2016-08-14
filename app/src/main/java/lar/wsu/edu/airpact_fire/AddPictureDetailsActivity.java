package lar.wsu.edu.airpact_fire;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

// User provides picture details before posting/queueing
public class AddPictureDetailsActivity extends AppCompatActivity {

    private Button mRetakeButton, mViewImageButton, mAddToQueueButton, mSubmitButton;
    private TextView mImageDateTextView, mImageLocationTextView,
            mLocationHeaderText, mVisualRangeHeaderText;
    private EditText mVisualRangeInput, mDescriptionInput, mAddTagInput;
    private Spinner mMetricSelectSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_picture_details);
        // TODO: Setup back button for this activity such that it goes back to selecting contrast points, not taking new picture!
        Util.setupSecondaryNavBar(this, SelectContrastActivity.class, "ENTER PICTURE DETAILS");

        // Set post context and activity
        Post.Context = getApplicationContext();
        Post.Activity = this;

        // Get UI elements
        mLocationHeaderText = (TextView) findViewById(R.id.location_header_text);
        mVisualRangeHeaderText = (TextView) findViewById(R.id.visual_range_header_text);
        mRetakeButton = (Button) findViewById(R.id.retake_button);
        mViewImageButton = (Button) findViewById(R.id.view_image_button);
        mAddToQueueButton = (Button) findViewById(R.id.add_to_queue_button);
        mSubmitButton = (Button) findViewById(R.id.submit_button);
        mImageDateTextView = (TextView) findViewById(R.id.date_time_text_view);
        mImageLocationTextView = (TextView) findViewById(R.id.location_text_view);
        mVisualRangeInput = (EditText) findViewById(R.id.visual_range_input);
        mDescriptionInput = (EditText) findViewById(R.id.description_input);
        mAddTagInput = (EditText) findViewById(R.id.add_tag_input);
        mMetricSelectSpinner = (Spinner) findViewById(R.id.metric_select_spinner);

        // Pre-loaded information
        populateFormData();

        // Event listeners
        mRetakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SelectContrastActivity.class);
                startActivity(intent);
            }
        });
        // View image with new activity
        mViewImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.storeTransactionImage(getApplicationContext(),
                        Util.stringToBitMap(
                                String.valueOf(UserDataManager.getUserData(UserDataManager.getRecentUser(), "image")
                                )));
                Intent intent = new Intent(getApplicationContext(), ViewImageActivity.class);
                // Pass in image to new activity
                //intent.putExtra("IMAGE_STRING", String.valueOf(UserDataManager.getUserData(UserDataManager.getRecentUser(), "image")));
                startActivity(intent);
            }
        });
        mAddToQueueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFormDataValid()) return;

                collectFormData();

                // Queue post
                Post post = new Post();
                post.queue(getApplicationContext());
            }
        });
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFormDataValid()) return;

                // Get data
                collectFormData();

                // Submit post
                Post post = new Post();
                post.submit(getApplicationContext());
            }
        });
    }

    // Check if form data is valid
    private boolean isFormDataValid() {

        boolean isValid = true;

        if (!Util.doesContainText(mImageLocationTextView)) {
            mLocationHeaderText.setTextColor(getResources().getColor(R.color.schemeRedHighlight));
            isValid = false;
        } else {
            mLocationHeaderText.setTextColor(getResources().getColor(R.color.schemeDark));
        }

        // TODO: The rest of the fields

        return isValid;
    }

    // Collect user values for post
    private void collectFormData() {
        String lastUser = UserDataManager.getRecentUser();
        UserDataManager.setUserData(lastUser, "description", mDescriptionInput.getText().toString());
//        UserDataManager.setUserData(lastUser, "visualRange", mVisualRangeInput.getText().toString());
        UserDataManager.setUserData(lastUser, "tags", mAddTagInput.getText().toString());
    }

    // Add past user values to form
    private void populateFormData() {
        String lastUser = UserDataManager.getRecentUser();
        mImageDateTextView.setText(UserDataManager.getUserData(lastUser, "loginTime"));
        mImageLocationTextView.setText("(" + UserDataManager.getUserData(lastUser, "geoX")
                + ", " + UserDataManager.getUserData(lastUser, "geoY") + ")");
        mAddTagInput.setText(UserDataManager.getUserData(lastUser, "tags"));
        mVisualRangeInput.setText(UserDataManager.getUserData(lastUser, "visualRange"));
        mDescriptionInput.setText(UserDataManager.getUserData(lastUser, "description"));
        // Spinner stuff
        List<String> metricOptions = new ArrayList<>();
        metricOptions.add("Feet");
        metricOptions.add("Meters");
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_text, metricOptions);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mMetricSelectSpinner.setAdapter(adapter);
    }
}
