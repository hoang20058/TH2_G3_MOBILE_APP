package com.example.studyschedule.activities;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.studyschedule.R;
import com.example.studyschedule.dal.StudentDAO;
import com.example.studyschedule.dal.StudentModuleDemoData;
import com.example.studyschedule.models.Student;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class StudentModuleUiTest {
    private UiAutomation automation;

    @Test public void demoSupportsCreateSearchEditDetailsAndDelete() throws Exception {
        automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        AccessibilityServiceInfo info = automation.getServiceInfo();
        info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        automation.setServiceInfo(info);
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        StudentDAO dao = StudentModuleDemoData.createDao(context);
        int initialCount = dao.getStudentCount();
        String key = "UI" + System.nanoTime();
        Intent intent = new Intent(context, StudentModuleDemoActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try (ActivityScenario<StudentModuleDemoActivity> screen = ActivityScenario.launch(intent)) {
            waitForCount(screen, initialCount);
            screenshot(context, "student-module-list.png");
            clickId("btnAddStudent");
            setText("edtDialogStudentCode", key);
            setText("edtDialogFullname", "Nguyễn Thị Ánh");
            assertEquals("Nguyễn Thị Ánh", node("edtDialogFullname", true).getText().toString());
            setText("edtDialogUsername", key);
            setText("edtDialogPassword", "secret");
            screenshot(context, "student-module-form.png");
            clickText("Lưu");
            waitForCount(screen, initialCount + 1);
            setText("edtSearchStudent", key);
            waitForCount(screen, 1);
            clickId("btnEditStudent");
            setText("edtDialogFullname", "Trần Hồng Ánh");
            clickText("Lưu");
            await(screen, activity -> {
                RecyclerView.ViewHolder holder = ((RecyclerView) activity.findViewById(R.id.rvStudents))
                        .findViewHolderForAdapterPosition(0);
                return holder != null && "Trần Hồng Ánh".contentEquals(
                        ((android.widget.TextView) holder.itemView.findViewById(R.id.tvStudentCodeAndName)).getText());
            });
            screen.onActivity(activity -> {
                RecyclerView.ViewHolder holder = ((RecyclerView) activity.findViewById(R.id.rvStudents))
                        .findViewHolderForAdapterPosition(0);
                assertNotNull(holder);
                holder.itemView.performClick();
            });
            assertNotNull(node("Trần Hồng Ánh", false));
            assertEquals(key, dao.getUsername(dao.searchStudents(key).get(0).getId()));
            assertTrue(automation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK));
            clickId("btnDeleteStudent");
            clickText("Hủy");
            assertEquals(initialCount + 1, dao.getStudentCount());
            clickId("btnDeleteStudent");
            clickText("Xóa");
            waitForCount(screen, 0);
            assertNotNull(node("tvEmptyStudents", true));
            assertFalse(dao.isUsernameTaken(key));
            setText("edtSearchStudent", "");
            waitForCount(screen, initialCount);
        } finally {
            for (Student student : dao.searchStudents(key)) {
                if (key.equals(student.getStudentCode())) dao.deleteStudent(student);
            }
        }
    }

    // Accessibility actions use the actual UI, without the removed InputManager API
    // used by the project's older Espresso dependency on Android 17.
    private AccessibilityNodeInfo node(String value, boolean byId) throws Exception {
        for (int i = 0; i < 60; i++) {
            AccessibilityNodeInfo root = automation.getRootInActiveWindow();
            if (root != null) {
                List<AccessibilityNodeInfo> matches = byId
                        ? root.findAccessibilityNodeInfosByViewId("com.example.studyschedule:id/" + value)
                        : root.findAccessibilityNodeInfosByText(value);
                if (!byId) {
                    for (AccessibilityNodeInfo match : matches) {
                        if (value.equalsIgnoreCase(match.getText() == null ? "" : match.getText().toString())) return match;
                    }
                }
                if (byId && !matches.isEmpty()) return matches.get(0);
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Missing UI element: " + value);
    }
    private void clickId(String id) throws Exception { clickNode(node(id, true)); }
    private void clickText(String text) throws Exception { clickNode(node(text, false)); }
    private void clickNode(AccessibilityNodeInfo target) {
        while (target != null && !target.isClickable()) target = target.getParent();
        assertNotNull(target);
        assertTrue(target.performAction(AccessibilityNodeInfo.ACTION_CLICK));
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }
    private void setText(String id, String text) throws Exception {
        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        assertTrue(node(id, true).performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args));
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }
    private void waitForCount(ActivityScenario<StudentModuleDemoActivity> screen, int count) throws Exception {
        await(screen, activity -> ((RecyclerView) activity.findViewById(R.id.rvStudents))
                .getAdapter().getItemCount() == count);
    }
    private interface Condition { boolean met(StudentModuleDemoActivity activity); }
    private void await(ActivityScenario<StudentModuleDemoActivity> screen, Condition condition) throws Exception {
        for (int i = 0; i < 80; i++) {
            AtomicBoolean ready = new AtomicBoolean();
            screen.onActivity(activity -> ready.set(condition.met(activity)));
            if (ready.get()) { InstrumentationRegistry.getInstrumentation().waitForIdleSync(); return; }
            Thread.sleep(100);
        }
        fail("UI did not finish updating within 8 seconds");
    }
    private void screenshot(Context context, String name) throws Exception {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Thread.sleep(350); // Let floating labels and dialog transitions finish before visual inspection.
        Bitmap image = automation.takeScreenshot();
        assertNotNull(image);
        try (FileOutputStream stream = new FileOutputStream(new File(context.getExternalFilesDir(null), name))) {
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        } finally { image.recycle(); }
    }
}
