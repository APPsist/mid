package de.appsist.service.mid;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User
{

    // user id
    public int id;

    // users first name
    private String firstname = "";
    // users middle name
    private String middlename = "";
    // users last name
    private String lastname = "";
    // users date of birth
    private Date dateofbirth = null;
    // users employer
    private String employer = "";
    // employeenumber
    private String employeenumber = "";

    // users current level of training
    private String traininglevel = "";

    // users mother tongue
    private String mothertongue = "";

    // competency levels
    final private String COMPLVLLOW = "Kenner";
    final private String COMPLVLMEDIUM = "Könner";
    final private String COMPLVLHIGH = "Experte";

    // map of competencylevels for activities Map<String, String>
    // String#1 denotes activityId
    // String#2 one of (COMPLVLLOW, COMPLVLMEDIUM, COMPLVLHIGH)
    private Map<String, String> activitiesCompLvl;

    // map of competencylevels for measures Map<String, String>
    // String#1 denotes measureId
    // String#2 one of (COMPLVLLOW, COMPLVLMEDIUM, COMPLVLHIGH)
    private Map<String, String> measuresCompLvl;

    // viewed content map Map<String, Integer>
    // String denotes contentId
    // Integer is the number of views
    private Map<String, Integer> contentViewed;

    public User()
    {
        // default constructor
        this.activitiesCompLvl = new HashMap<String, String>();
        this.measuresCompLvl = new HashMap<String, String>();
        this.contentViewed = new HashMap<String, Integer>();

    }

    // returns user id
    public int getId()
    {
        return id;
    }

    // set user id
    public void setId(int id)
    {
        this.id = id;
    }

    // returns users firstname
    public String getFirstname()
    {
        return firstname;
    }

    // set users firstname
    public void setFirstname(String firstname)
    {
        this.firstname = firstname;
    }

    // returns users middlename
    public String getMiddlename()
    {
        return middlename;
    }

    // set users middlename
    public void setMiddlename(String middlename)
    {
        this.middlename = middlename;
    }

    // returns users lastname
    public String getLastname()
    {
        return lastname;
    }

    // set users lastname
    public void setLastname(String lastname)
    {
        this.lastname = lastname;
    }

    // returns date of birth
    public Date getDateofbirth()
    {
        return dateofbirth;
    }

    // set date of birth
    public void setDateofbirth(Date dateofbirth)
    {
        this.dateofbirth = dateofbirth;
    }

    // returns name of employer
    public String getEmployer()
    {
        return employer;
    }

    // set employer
    public void setEmployer(String employer)
    {
        this.employer = employer;
    }

    // returns employeenumber
    public String getEmployeenumber()
    {
        return employeenumber;
    }

    // set employee number
    public void setEmployeenumber(String employeenumber)
    {
        this.employeenumber = employeenumber;
    }

    // returns the current traininglevel of user
    public String getTraininglevel()
    {
        return traininglevel;
    }

    // set the current traininglevel of user
    public void setTraininglevel(String traininglevel)
    {
        this.traininglevel = traininglevel;
    }

    // returns mother tongue
    public String getMothertongue()
    {
        return mothertongue;
    }

    // set mother tongue
    public void setMothertongue(String mothertongue)
    {
        this.mothertongue = mothertongue;
    }

    // returns map with all levels for all activities
    public Map<String, String> getActivitiesCompLvls()
    {
        return activitiesCompLvl;
    }

    // return competencylevel for specified activity
    public String getActivityCompLvl(String activityId)
    {
        if (this.activitiesCompLvl.containsKey(activityId)) {
            return this.activitiesCompLvl.get(activityId);
        }
        return COMPLVLLOW;
    }

    // set competencylevel for specified activity
    public void setActivityCompLvl(String activityId, String lvl)
    {
        this.activitiesCompLvl.put(activityId, lvl);
    }

    // return map with competencylevels for all measures
    public Map<String, String> getMeasuresCompLvls()
    {
        return measuresCompLvl;
    }

    // return competencylevel for specified measure
    public String getMeasureCompLvl(String measureId)
    {
        if (this.measuresCompLvl.containsKey(measureId)) {
            return this.measuresCompLvl.get(measureId);
        }
        return COMPLVLLOW;
    }

    // set competencylevel for specified measure
    public void setMeasureCompLvl(String measureId, String lvl)
    {
        this.measuresCompLvl.put(measureId, lvl);
    }

    // returns map with amount of views for all content items
    public Map<String, Integer> getContentViewed()
    {
        return contentViewed;
    }

    // set amount of views for multiple content items
    public void setContentViewed(Map<String, Integer> contentViewed)
    {
        this.contentViewed = contentViewed;
    }

    // returns number of views for specified content item or -1 if we have a new item
    public int getNumContentViews(String contentItemId)
    {
        if (this.contentViewed.containsKey(contentItemId)) {
            return contentViewed.get(contentItemId);
        }
        return -1;
    }

    // increases number of views for specified content item
    public void contentViewed(String contentItemId)
    {
        int numContentView = this.getNumContentViews(contentItemId);
        if (numContentView < 0) {
            numContentView = 0;
        }

        this.contentViewed.put(contentItemId, ++numContentView);
    }

}
