package by.bstu.vs.stpms.lablist.model;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import by.bstu.vs.stpms.lablist.model.dao.LabDao;
import by.bstu.vs.stpms.lablist.model.dao.SubjectDao;
import by.bstu.vs.stpms.lablist.model.dao.TermDao;
import by.bstu.vs.stpms.lablist.model.entity.Lab;
import by.bstu.vs.stpms.lablist.model.entity.Subject;
import by.bstu.vs.stpms.lablist.model.entity.Term;

@Database(entities = {Lab.class, Subject.class, Term.class}, version = 13)
public abstract class LabDatabase extends RoomDatabase {

    public abstract LabDao getLabDao();
    public abstract SubjectDao getSubjectDao();
    public abstract TermDao getTermDao();

    private static volatile LabDatabase INSTANCE;

    public static LabDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LabDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            LabDatabase.class, "lab_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
