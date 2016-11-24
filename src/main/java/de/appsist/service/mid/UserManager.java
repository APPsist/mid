package de.appsist.service.mid;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserManager
{
    private static UserManager _instance = new UserManager();
    private Map<Integer, User> userList;
    private int nextUserId = 1;

    public static UserManager getInstance()
    {
        return _instance;
    }

    private UserManager()
    {
        userList = new HashMap<Integer, User>();
        addSomeUsers();
    }

    public User getUser(int userId)
    {
        if (this.userList.containsKey(userId)) {
            return this.userList.get(userId);
        }
        return null;
    }

    // create a new user
    public void createUser(String fn, String ln, String emp)
    {
        int uId = this.nextUserId++;
        User newUser = new User();
        newUser.setId(uId);
        newUser.setFirstname(fn);
        newUser.setLastname(ln);
        newUser.setEmployer(emp);
        newUser.setEmployeenumber("MA" + uId);
        newUser.setDateofbirth(new Date());
        this.userList.put(uId, newUser);
    }

    // delete a user
    public void deleteUser(int userId)
    {
        this.userList.remove(userId);
    }

    // returns the next user id
    public int getNextUserId()
    {
        return this.nextUserId;
    }

    // create some users to play with
    private void addSomeUsers()
    {
        createUser("Emil", "Emsig", "Festo");
        createUser("Emelie", "Exakt", "MBB");
        createUser("Julia", "Just", "MBB");
        createUser("Willi", "Wichtig", "Festo");
        createUser("Stefan", "Schlosser", "Festo");
        createUser("Fritz", "Fräse", "Brabant & Lehnert");
        createUser("John", "Doe", "Brabant & Lehnert");
        System.out.println("Users created");
    }

    // list contents seen by user
    public String listContentsSeen(int userId)
    {
        String resultHTML = "<table>";
        User u = getUser(userId);
        for (String str : u.getContentViewed().keySet()) {
            resultHTML += "<tr><td>" + str + "</td><td>" + u.getContentViewed().get(str)
                    + "</td></tr>";
        }
        resultHTML += "</table>";
        return resultHTML;

    }
}
