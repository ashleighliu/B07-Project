package com.example.b07_project;

import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminCourseAdapter extends RecyclerView.Adapter<AdminCourseAdapter.MyViewHolder> {
    Context myContext;
    ArrayList<Course> course_list;
    DatabaseReference fire;
    private List<ClipData.Item> itemList;

    public AdminCourseAdapter(Context context, ArrayList<Course> course) {
        myContext = context;
        course_list = course;
    }

    public void setFilteredList(ArrayList<Course> filteredList){
        this.course_list = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(myContext).inflate(R.layout.course_info_admin, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Course course = course_list.get(position);
        holder.course_code.setText(course.getCourseCode());
        holder.course_name.setText(course.getCourseName());

    }

    @Override
    public int getItemCount() {
        return course_list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView course_name, course_code;
        ImageButton edit, delete;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            course_name = itemView.findViewById(R.id.adminCourseName);
            course_code = itemView.findViewById(R.id.adminCourseCode);
            edit  = itemView.findViewById(R.id.editCourse);
            delete = itemView.findViewById(R.id.deleteCourse);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fire = FirebaseDatabase.getInstance().getReference();
                    String courseID = (course_list.get(getAdapterPosition())).getCourseID();
                    deleteFromCourses(courseID);
                    deleteFromStudent(courseID);
                    course_list.remove(getAdapterPosition());
                    notifyDataSetChanged();

                }
            });


            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String courseID = (course_list.get(getAdapterPosition())).getCourseID();
                    String courseName = (course_list.get(getAdapterPosition())).getCourseName();
                    String courseCode = (course_list.get(getAdapterPosition())).getCourseCode();
                    ArrayList<String> offeringSessions = (course_list.get(getAdapterPosition())).getOfferingSessions();
                    ArrayList<String> preReqs = (course_list).get(getAdapterPosition()).getPrereqs();
                    DatabaseReference getPreCodes = FirebaseDatabase.getInstance().getReference().child("Courses");
                    getPreCodes.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ArrayList<String> codes = new ArrayList<>();
                            for(int i = 0; i < preReqs.size(); i++)
                            {
                                Log.i("masda", preReqs.get(i));
                                codes.add(snapshot.child(preReqs.get(i)).child("courseCode").getValue(String.class));
                            }
                            getPreCodes.removeEventListener(this);
                            AppCompatActivity activity = (AppCompatActivity) view.getContext();
                            Fragment course = new EditCourse();
                            Bundle information = new Bundle();
                            information.putStringArrayList("prereqs", codes);
                            Log.i("myTag", preReqs.get(0));
                            information.putString("courseName", courseName);
                            information.putString("courseCode", courseCode);
                            information.putString("courseID", courseID);
                            information.putStringArrayList("offeringSessions", offeringSessions);
                            course.setArguments(information);
                            FragmentManager fragmentManager = activity.getSupportFragmentManager();
                            fragmentManager.beginTransaction().replace(R.id.admin_frame, course).commit();
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
            });

        }
    }
    public void deleteFromStudent(String courseID)
    {
        DatabaseReference students = fire.child("Accounts");
        students.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot x : snapshot.getChildren()) {
                    if (x.child("isAdmin").getValue(Boolean.class).equals(true)) {
                        break;
                    } else {
                        ArrayList<String> courses = new ArrayList<>();
                        DataSnapshot here = x.child("Courses_taken");
                        for (DataSnapshot y : here.getChildren()) {
                            if (!y.getValue(String.class).equals(courseID)) {
                                courses.add(y.getValue(String.class));
                            }
                            students.child(x.getKey()).child("Courses_taken").setValue(courses);
                        }
                    }
                    students.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void deleteFromCourses(String courseID)
    {
        Log.i("myTag", courseID);
        DatabaseReference courses = fire.child("Courses");
        courses.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot x : snapshot.getChildren()) {
                    if (x.getKey().equals(courseID)) {
                        courses.child(x.getKey()).removeValue();

                    } else {
                        DataSnapshot goPrereq = x.child("prereqs");
                        ArrayList<String> temp_clone = new ArrayList<>();
                        for (DataSnapshot y : goPrereq.getChildren()) {
                            if (!(y.getValue(String.class).equals(courseID))) {
                                temp_clone.add(y.getValue(String.class));
                                Log.i("Size", String.valueOf(temp_clone.size()));
                            }
                        }
                        nullers(temp_clone);
                        courses.child(x.getKey()).child("prereqs").setValue(temp_clone);
                    }
                }
                courses.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void nullers(ArrayList<String> check){
        if(check.size() == 0){
            check.add("null");
        }
    }

}
