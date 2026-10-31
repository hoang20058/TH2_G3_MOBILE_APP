package com.example.studyschedule.activities;

import com.example.studyschedule.dal.StudentDAO;
import com.example.studyschedule.dal.StudentModuleDemoData;

/** Debug-only entry point. Never creates a login session or accesses the main database. */
public class StudentModuleDemoActivity extends StudentManagementActivity {
    @Override protected boolean hasModuleAccess() { return true; }
    @Override protected StudentDAO createStudentDao() {
        return StudentModuleDemoData.createDao(this);
    }
}
