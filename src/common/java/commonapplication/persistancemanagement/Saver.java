package commonapplication.persistancemanagement;

import commonapplication.models.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;

public class Saver {


    //saves data to existing file, if it doesn't exist,
    // it creates a new one and writes the data to it
    // make sure to pass an id >= 0 if you want to modify
    // an individual Restaurant, and -1 if you want to add/
    // modify a restaurant short data in Restaurants.dat
    // and -2 if you want to save into the Users.dat file
    public static void saveToFile(String path, String data, int config) {
        String str = "";
        switch (config) {
            case 0 -> str = path;
            case 1 -> str = "src/server/resources/Restaurants.dat";
            case 2 -> str = "src/server/resources/Users.dat";
            case 3 -> str = "src/server/resources/Usernames.dat";
        }

        PrintWriter pw;
        try {
            pw = new PrintWriter(str);
            pw.println(data);
            pw.flush();
            pw.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private static void createNewFile(Object object, int config) {
        PrintWriter pw;
        try {
            if (object instanceof Restaurant) {
                Restaurant restaurant = (Restaurant) object;
                if (config == 0 || config == 2) {
                    pw = new PrintWriter(Generator.generateFileName(restaurant));
                    pw.println(restaurant.toString());
                    pw.flush();
                    pw.close();
                }
                if (config == 1 || config == 2) {
                    pw = new PrintWriter("src/server/resources/RestaurantIDs.dat");
                    pw.println(Generator.generateFileName(restaurant));
                    pw.flush();
                    pw.close();
                }
            } else if (object instanceof User) {
                User user = (User) object;
                if (config == 0 || config == 2) {
                    pw = new PrintWriter("src/server/resources/Users.dat");
                    pw.println(user.toString());
                    pw.flush();
                    pw.close();
                }
                if (config == 1 || config == 2) {
                    pw = new PrintWriter("src/server/resources/Usernames.dat");
                    pw.println(user.getUsername());
                    pw.flush();
                    pw.close();
                }
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException();
        }
    }

    public static void addReservation(Reservation reservation) {
        modifyData(reservation, 0);
    }

    public static void removeReservation(Reservation reservation) {
        modifyData(reservation, 1);
    }

    private static boolean modifyData(Object object, int config) {
        if (object instanceof Reservation) {
            Reservation reservation = (Reservation) object;
            File restFile = reservation.getRestaurant().getRestaurantFile();
            if (!restFile.exists()) {
                createNewFile(reservation.getRestaurant(), -1);
                return true;
            }
            String restaurantData = DataHandler.readFile(restFile);
            String reservationData = reservation.toString();
            if (config == 1) {
                restaurantData = restaurantData.replaceFirst(reservationData, "");
            } else {
                if (!restaurantData.contains(reservationData))
                    restaurantData = restaurantData.concat(reservationData);
            }
            saveToFile(Generator.generateFileName(reservation.getRestaurant()), restaurantData, 1);
            return true;
        } else if (object instanceof User) {
            User user = (User) object;
            if (config == 0) {
                if (Parser.userExists(user.getUsername())) {
                    //If user already exists, nothing happens. He is told that this username is already taken
                    // TODO : this should be implemented on client (COMMENT)
                    return false;
                } else {
                    File file1 = new File("src/server/resources/Usernames.dat");
                    File file2 = new File("src/server/resources/Users.dat");
                    boolean f1e = file1.exists();
                    boolean f2e = file2.exists();
                    if (!f1e || !f2e) {
                        if (!f1e && !f2e) {
                            createNewFile(user, 2);
                        } else if (!f1e) {
                            createNewFile(user, 1);
                            String usersData = DataHandler.readFile(file2);
                            usersData = usersData.concat(user.toString());
                            saveToFile("", usersData, 2);
                        } else { //!f2e
                            createNewFile(user, 0);
                            String usernames = DataHandler.readFile(file1);
                            usernames = usernames.concat(user.getUsername() + ",");
                            saveToFile("", usernames, 3);
                        }
                    } else {
                        String usersData = DataHandler.readFile(file2);
                        usersData = usersData.concat(user.toString());
                        saveToFile("", usersData, 2);
                        String usernames = DataHandler.readFile(file1);
                        usernames = usernames.concat(user.getUsername() + ",");
                        saveToFile("", usernames, 3);
                    }
                }
                return true;
            } else if (config == -1) {
                File file1 = new File("src/server/resources/Usernames.dat");
                File file2 = new File("src/server/resources/Users.dat");
                boolean f1e = file1.exists();
                boolean f2e = file2.exists();
                if (!f1e || !f2e) {
                    if (!f1e && !f2e) {
                        //this case is where someone deletes the files, the account is not deleted,
                        // but is as good as deleted because the files don't exist anyway
                    } else if (!f1e) {
                        String usersData = DataHandler.readFile(file2);
                        usersData = usersData.replaceFirst(user.toString(), "");
                        saveToFile("", usersData, 2);
                    } else {
                        String usernames = DataHandler.readFile(file1);
                        usernames = usernames.replaceFirst(user.getUsername() + ",", "");
                        saveToFile("", usernames, 3);
                    }
                } else {
                    String userData = DataHandler.readFile(file2);
                    String usernames = DataHandler.readFile(file1);
                    userData = userData.replaceFirst(user.toString(), "");
                    usernames = usernames.replaceFirst(user.getUsername() + ",", "");
                    saveToFile("", userData, 2);
                    saveToFile("", usernames, 3);
                }
                return true;
                //true here just stands for the non-existence of the user in the files after this method is executed
            } else {
                if (!Parser.userExists(user.getUsername())) {
                    //If user doesn't already exist, nothing happens and you can't modify any data.
                    // False is returned and should be handled by prompting the user to create a new account
                    //or an appropriate message
                    //Again this should not normally happen, because only a logged in user can change his password,
                    //it will happen however, if someone deleted the usernames file
                    // TODO : this should be implemented on client (COMMENT)
                    return false;
                } else {
                    String strToReplace = Parser.getUserInfoByUsername(user.getUsername());
                    File file0 = new File("src/server/resources/Users.dat");
                    if (!file0.exists()) {
                        return false;
                    }
                    String userData = DataHandler.readFile(file0);
                    userData = userData.replaceFirst(strToReplace, user.getUserInfo());
                    saveToFile("", userData, 2);
                    return true;
                }
            }
        }
        return true;
    }


    public static boolean addUser(User user) {
        return modifyData(user, 0);
    }

    // this deletes a user and all his reservations from the save files
    public static boolean deleteUser(User user) {
        // Some existence checks were omitted inside modifyData ,
        // because technically you are only able to delete a user if you are logged in as that user,
        // which implies it already exists, same applies for the modifyUser method.
        return modifyData(user, -1);
    }

    // This can only modify the Password. user.setPassword(String password) should be called which then modifies the password
    // on the user instance, setPassword() will then call this method and change the new password to the file
    public static boolean modifyUser(User user) {
        return modifyData(user, -2);
    }

    /*public static void modifyUserData(User user) {
        if (!file.exists()) {
            createNewFile(user.toString());
        }
    }*/


    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant(1, "L'Osteria", LocalTime.NOON, LocalTime.MIDNIGHT, 3, 3, Speciality.Pizza, "Somewhere");
        Restaurant restaurant1 = new Restaurant(2, "L'Osteria", LocalTime.NOON, LocalTime.MIDNIGHT, 3, 3, Speciality.Pizza, "Somewhere");
        File file = new File(Generator.generateFileName(restaurant));
        File file1 = new File(Generator.generateFileName(restaurant1));
        User user = new User("Chiheb", "goodpass".hashCode());
        Table table = new Table(1L, restaurant);
        Table table1 = new Table(1L, restaurant1);
        Reservation reservation = new Reservation(LocalTime.MIN, LocalTime.MAX, "CHIHEB", restaurant, table, LocalDate.now());
        Reservation reservation1 = new Reservation(LocalTime.MIN, LocalTime.MAX, "MAHA", restaurant, table, LocalDate.now());
        Saver.modifyData(reservation, 0);
        Saver.modifyData(reservation1, 0);
        Reservation reservation2 = new Reservation(LocalTime.MIN, LocalTime.MAX, "NIKLAS", restaurant1, table1, LocalDate.now());
        Reservation reservation3 = new Reservation(LocalTime.MIN, LocalTime.MAX, "PRAMOD", restaurant1, table1, LocalDate.now());
        Saver.modifyData(reservation2, 0);
        Saver.modifyData(reservation3, 0);
        System.out.println(file.exists());
        System.out.println(Parser.getRestaurantFromFile(file).toString());
    }

    /*public static void main(String[] args) {
        File file = new File("src/server/resources/Restaurants/1.dat");
        createNewRestaurantFile(file);
        //saveToFile(1,"oogaBooga");
        //saveToFile(2,"x");
        System.out.println(DataHandler.readFile(file));
        Restaurant restaurant = new Restaurant(1,"rest1", LocalTime.NOON,LocalTime.MIDNIGHT,0,0);
        Table table = new Table(1,restaurant);
        Reservation reservation = new Reservation(LocalTime.MIN,LocalTime.MAX,1,restaurant,1,table);
        modifyReservation(file,reservation);
    }*/

}