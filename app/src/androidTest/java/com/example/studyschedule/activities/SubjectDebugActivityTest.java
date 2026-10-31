package com.example.studyschedule.activities;

import android.content.Context;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.studyschedule.R;
import com.example.studyschedule.dal.SubjectDAO;
import com.example.studyschedule.models.Subject;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.Map;
import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class SubjectDebugActivityTest {
    @Test public void addFormShowsInlineErrorsAndCancelDoesNotInsert() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SubjectDAO dao = new SubjectDAO(context);
        int before = dao.getAllSubjects().size();
        try (ActivityScenario<SubjectDebugActivity> scenario = ActivityScenario.launch(SubjectDebugActivity.class)) {
            onView(withId(R.id.btnAddSubject)).perform(click());
            onView(withId(R.id.tvSubjectFormTitle)).check(matches(withText(R.string.subject_form_add)));
            onView(withId(R.id.edtDialogCredits)).perform(scrollTo(), replaceText("2147483648"), closeSoftKeyboard());
            onView(withId(android.R.id.button1)).perform(click());
            onView(withId(R.id.tilSubjectCode)).check(matches(hasDescendant(withText(R.string.subject_required))));
            onView(withId(R.id.tilSubjectName)).check(matches(hasDescendant(withText(R.string.subject_required))));
            onView(withId(R.id.tilSubjectCredits)).check(matches(hasDescendant(withText(R.string.subject_invalid_credits))));
            onView(withId(android.R.id.button2)).perform(click());
        }
        assertEquals(before, dao.getAllSubjects().size());
    }

    @Test public void editFormPrefillsAndCancelKeepsOriginalData() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SubjectDAO dao = new SubjectDAO(context);
        Subject subject = dao.getAllSubjects().get(0);
        try (ActivityScenario<SubjectDebugActivity> scenario = ActivityScenario.launch(SubjectDebugActivity.class)) {
            scenario.onActivity(activity -> activity.onEdit(subject));
            onView(withId(R.id.tvSubjectFormTitle)).check(matches(withText(R.string.subject_edit)));
            onView(withId(R.id.edtDialogSubjectCode)).check(matches(withText(subject.getSubjectCode())));
            onView(withId(R.id.edtDialogCredits)).check(matches(withText(String.valueOf(subject.getCredits()))));
            onView(withId(R.id.edtDialogSubjectName)).perform(scrollTo(), replaceText("Tên chưa lưu"), closeSoftKeyboard());
            onView(withId(android.R.id.button2)).perform(click());
        }
        Subject after = dao.searchSubjects(subject.getSubjectCode()).get(0);
        assertEquals(subject.getId(), after.getId());
        assertEquals(subject.getSubjectName(), after.getSubjectName());
    }

    @Test public void opensModuleWithoutChangingSharedLoginSession() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Map<String, ?> before = context.getSharedPreferences(
                "StudyScheduleSessionPrefs", Context.MODE_PRIVATE).getAll();
        try (ActivityScenario<SubjectDebugActivity> scenario = ActivityScenario.launch(SubjectDebugActivity.class)) {
            scenario.onActivity(activity -> {
                assertFalse(activity.isFinishing());
                assertNotNull(activity.findViewById(R.id.rvSubjects));
                assertNotNull(activity.findViewById(R.id.btnAddSubject));
            });
        }
        assertEquals(before, context.getSharedPreferences(
                "StudyScheduleSessionPrefs", Context.MODE_PRIVATE).getAll());
    }
}
