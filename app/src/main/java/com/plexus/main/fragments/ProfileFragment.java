package com.plexus.main.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.plexus.R;
import com.plexus.core.components.bottomsheet.adapter.SheetOptionsAdapter;
import com.plexus.core.components.bottomsheet.model.SheetOptions;
import com.plexus.model.posts.Post;
import com.plexus.model.account.User;
import com.plexus.posts.activity.saved_posts.SavedPostsActivity;
import com.plexus.account.FollowersActivity;
import com.plexus.account.activity.AccountStatusActivity;
import com.plexus.account.activity.EditProfileActivity;
import com.plexus.account.activity.FollowingActivity;
import com.plexus.account.activity.settings.ProfileLogActivity;
import com.plexus.account.adapters.ProfilePostAdapter;
import com.plexus.settings.activity.privacy.PrivacyActivity;
import com.plexus.utils.MasterCipher;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/******************************************************************************
 * Copyright (c) 2020. Plexus, Inc.                                           *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 *  limitations under the License.                                            *
 ******************************************************************************/

public class ProfileFragment extends Fragment {

    public ImageView verified, menu;
    StorageReference storageReference;
    private SimpleDraweeView profile_cover;
    TextView posts, fullname, bio, username;
    TextView followers, following;
    Button follow;
    private FirebaseUser firebaseUser;
    RecyclerView recycler_view;
    private List<Post> postList;
    private SimpleDraweeView image_profile;
    private BottomSheetDialog profile_sheet;
    private ProfilePostAdapter profilePostAdapter;

    public static final String[] titles = new String[]{"Profile Activity", "Account Privacy", "Account Status", "Saved Posts", "Profile Link"};
    public static final Integer[] images = {R.drawable.profile_activity, R.drawable.lock_outline, R.drawable.error, R.drawable.bookmark_multiple_outline, R.drawable.link_variant};

    ArrayList<SheetOptions> rowItems;

    @SuppressLint("CheckResult")
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("avatars/covers");

        image_profile = view.findViewById(R.id.profile_image);
        profile_cover = view.findViewById(R.id.image_cover);
        username = view.findViewById(R.id.username);
        posts = view.findViewById(R.id.posts);
        follow = view.findViewById(R.id.follow);
        fullname = view.findViewById(R.id.fullname);
        bio = view.findViewById(R.id.about);
        menu = view.findViewById(R.id.menu);
        verified = view.findViewById(R.id.verified);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        recycler_view = view.findViewById(R.id.recycler_view);

        recycler_view.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new GridLayoutManager(getContext(), 3);
        recycler_view.setLayoutManager(mLayoutManager);
        postList = new ArrayList<>();
        profilePostAdapter = new ProfilePostAdapter(getContext(), postList);
        recycler_view.setAdapter(profilePostAdapter);

        profile_sheet = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        profile_sheet.setContentView(R.layout.sheet_layout);
        ListView listView = profile_sheet.findViewById(R.id.listview);

        rowItems = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            SheetOptions item = new SheetOptions(titles[i], images[i]);
            rowItems.add(item);
        }

        SheetOptionsAdapter optionsAdapter = new SheetOptionsAdapter(getContext(), rowItems);
        listView.setAdapter(optionsAdapter);

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            if (position == 0){
                startActivity(new Intent(getContext(), ProfileLogActivity.class));
                profile_sheet.dismiss();
            }

            if (position == 1) {
                startActivity(new Intent(getContext(), PrivacyActivity.class));
                profile_sheet.dismiss();
            }

            if (position == 2) {
                startActivity(new Intent(getContext(), AccountStatusActivity.class));
                profile_sheet.dismiss();
            }

            if (position == 3) {
                startActivity(new Intent(getContext(), SavedPostsActivity.class));
                profile_sheet.dismiss();
            }

            if (position == 4){
                generateDynamicLink(generateDeepLinkUrl(firebaseUser.getUid()));
            }

        });

        menu.setOnClickListener(v -> profile_sheet.show());

        follow.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));
        profile_cover.setOnClickListener(v -> CropImage.activity().start(requireContext(), this));

        following.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FollowingActivity.class);
            intent.putExtra("profileid", firebaseUser.getUid());
            startActivity(intent);
        });

        followers.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FollowersActivity.class);
            intent.putExtra("profileid", firebaseUser.getUid());
            startActivity(intent);
        });

        getFollowers(following, followers);
        myFotos();
        getNrPosts();

        return view;
  }

  private void myFotos() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(firebaseUser.getUid())) {
                        postList.add(post);
                    }

                }
                Collections.reverse(postList);
                profilePostAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {

            }
        });
  }

  private void getNrPosts(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(firebaseUser.getUid())){
                        i++;
                    }
                }
                posts.setText(MessageFormat.format("{0}", i));
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {

            }
        });
  }

  private void getFollowers(TextView following, TextView followers) {
      DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(firebaseUser.getUid()).child("followers");
      reference.addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
              followers.setText(MessageFormat.format("{0}", dataSnapshot.getChildrenCount()));
          }

          @Override
          public void onCancelled(@NotNull DatabaseError databaseError) {

          }
      });

      DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Follow").child(firebaseUser.getUid()).child("following");
      reference1.addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
              following.setText(MessageFormat.format("{0}", dataSnapshot.getChildrenCount()));
          }

          @Override
          public void onCancelled(@NotNull DatabaseError databaseError) {

          }
      });
  }

  /*
  * Get user information from Firebase or anything related to Firebase.
  */

  private void userInformation(){
      DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
      ValueEventListener valueEventListener = new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
              User user = snapshot.getValue(User.class);

              fullname.setText(MessageFormat.format("{0} {1}", MasterCipher.decrypt(user.getName()), MasterCipher.decrypt(user.getSurname())));
              username.setText(MasterCipher.decrypt(user.getUsername()));
              bio.setText(MasterCipher.decrypt(user.getBio()));

              image_profile.setImageURI(MasterCipher.decrypt(user.getImageurl()));
              profile_cover.setImageURI(MasterCipher.decrypt(user.getProfile_cover()));

              if (user.isVerified()) {
                  verified.setVisibility(View.VISIBLE);
              } else {
                  verified.setVisibility(View.GONE);
              }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {

          }
      };
      databaseReference.addValueEventListener(valueEventListener);
  }

    private String generateDeepLinkUrl(String pushID) {
        return "https://plexus.dev/profile=" + pushID;
    }

    /**
     * This will return a shrinked link using Firebase Dynamic Links , this method will shrink this lik plexus.dev/groups=pushID
     * @param url of the custom page we created above with the custom data of the user
     */
    private void generateDynamicLink(String url) {
        // setDomainUriPrefix should host a link like this https://plexus.dev/groups
        // The androidParameters is just the package name of the app , this is because if the app is not installed it will prompt the user to the play store to download it, package example com.gaston.myapp
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(Uri.parse(url))
                        .setDomainUriPrefix("https://plexus.dev/profile")
                        .setAndroidParameters(
                                new DynamicLink.AndroidParameters.Builder("com.plexus")
                                        .setMinimumVersion(122)
                                        .build())
                        .setIosParameters(
                                new DynamicLink.IosParameters.Builder("com.plexus")
                                        .setAppStoreId("")
                                        .setMinimumVersion("1.0.0")
                                        .build())
                        .setSocialMetaTagParameters(
                                new DynamicLink.SocialMetaTagParameters.Builder()
                                        .setTitle(MasterCipher.decrypt(user.getName()) + " " + MasterCipher.decrypt(user.getSurname()))
                                        .setDescription(MasterCipher.decrypt(user.getBio()))
                                        .setImageUrl(Uri.parse(MasterCipher.decrypt(user.getImageurl())))
                                        .build())
                        .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                        .addOnCompleteListener(getActivity(), task -> {
                            if (task.isSuccessful()) {
                                // we get the dynamic link generated and pass it to the shareDeepLink method
                                Uri shortURL = task.getResult().getShortLink();
                                shareDeepLink(shortURL.toString());
                            } else {
                                Toast.makeText(getContext(), "Failed to create link", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void shareDeepLink(String url){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(shareIntent, "Share Profile via"));
    }

    @Override
    public void onStart() {
        super.onStart();
        userInformation();
    }
}
