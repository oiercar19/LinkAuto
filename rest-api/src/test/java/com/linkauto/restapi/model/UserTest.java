package com.linkauto.restapi.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserTest {

    private User user;
    private static final long BIRTH_DATE = 631152000L;

    @BeforeEach
    public void setUp() {
        List<String> cars = new ArrayList<>(Arrays.asList("Toyota", "BMW"));
        List<Post> posts = new ArrayList<>();
        List<User> followers = new ArrayList<>();
        List<User> following = new ArrayList<>();
        user = new User(
            "user1",
            "Alejandro Martinez",
            "profile.jpg",
            "mega@ejemplo.com",
            cars,
            BIRTH_DATE,
            User.Gender.MALE,
            "Bilbao",
            "password123",
            "Desc",
            posts,
            followers,
            following
        );
    }

    @Test
    public void testDefaultConstructor() {
        assertDoesNotThrow(() -> new User());
    }

    @Test
    public void testGettersAndSetters() {
        assertEquals("user1", user.getUsername());
        assertEquals(Role.USER, user.getRole());
        assertEquals("Alejandro Martinez", user.getName());
        assertEquals("profile.jpg", user.getProfilePicture());
        assertEquals("mega@ejemplo.com", user.getEmail());
        assertEquals(Arrays.asList("Toyota", "BMW"), user.getCars());
        assertEquals(BIRTH_DATE, user.getBirthDate());
        assertEquals(User.Gender.MALE, user.getGender());
        assertEquals("Bilbao", user.getLocation());
        assertEquals("password123", user.getPassword());
        assertEquals("Desc", user.getDescription());

        user.setUsername("user2");
        user.setRole(Role.ADMIN);
        user.setName("Alejandro Garcia");
        user.setProfilePicture("newpic.jpg");
        user.setEmail("garcia@ejempo.com");
        List<String> newCars = Arrays.asList("Audi");
        user.setCars(newCars);
        long newBirthDate = 123456789L;
        user.setBirthDate(newBirthDate);
        user.setGender(User.Gender.FEMALE);
        user.setLocation("LA");
        user.setPassword("newpass");
        user.setDescription("New Desc");

        assertEquals("user2", user.getUsername());
        assertEquals(Role.ADMIN, user.getRole());
        assertEquals("Alejandro Garcia", user.getName());
        assertEquals("newpic.jpg", user.getProfilePicture());
        assertEquals("garcia@ejempo.com", user.getEmail());
        assertEquals(newCars, user.getCars());
        assertEquals(newBirthDate, user.getBirthDate());
        assertEquals(User.Gender.FEMALE, user.getGender());
        assertEquals("LA", user.getLocation());
        assertEquals("newpass", user.getPassword());
        assertEquals("New Desc", user.getDescription());
    }

    @Test
    public void testPostOperations() {
        assertTrue(user.getPosts().isEmpty());
        Post post = new Post();
        user.addPost(post);
        assertEquals(1, user.getPosts().size());
        assertTrue(user.getPosts().contains(post));
    }

    @Test
    public void testFollowerOperations() {
        assertTrue(user.getFollowers().isEmpty());
        User follower = new User(
            "follower1",
            "Name",
            "pic",
            "e@mail",
            new ArrayList<>(),
            BIRTH_DATE,
            User.Gender.OTHER,
            "Loc",
            "pwd",
            "desc",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
        user.addFollower(follower);
        assertEquals(1, user.getFollowers().size());
        assertTrue(user.getFollowers().contains(follower));

        user.removeFollower(follower);
        assertTrue(user.getFollowers().isEmpty());
    }

    @Test
    public void testFollowingOperations() {
        assertTrue(user.getFollowing().isEmpty());
        User followingUser = new User(
            "follow1",
            "Name",
            "pic",
            "e@mail",
            new ArrayList<>(),
            BIRTH_DATE,
            User.Gender.OTHER,
            "Loc",
            "pwd",
            "desc",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
        user.addFollowing(followingUser);
        assertEquals(1, user.getFollowing().size());
        assertTrue(user.getFollowing().contains(followingUser));

        user.removeFollowing(followingUser);
        assertTrue(user.getFollowing().isEmpty());
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void testEqualsAndHashCode() {
        User u1 = new User(
            "sameUser",
            "A",
            "pic A",
            "a@mail",
            new ArrayList<>(),
            111L,
            User.Gender.MALE,
            "L1",
            "p1",
            "d1",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
        User u2 = new User(
            "sameUser",
            "B",
            "pic B",
            "b@mail",
            new ArrayList<>(),
            222L,
            User.Gender.FEMALE,
            "L2",
            "p2",
            "d2",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());

        User u3 = new User(
            "otherUser",
            "A",
            "pic A",
            "a@mail",
            new ArrayList<>(),
            111L,
            User.Gender.MALE,
            "L1",
            "p1",
            "d1",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
        u3.setRole(Role.ADMIN);

        assertNotEquals(u1, u3);
        assertNotEquals(u1.hashCode(), u3.hashCode());

        assertFalse(u1.equals(null));
        assertFalse(u1.equals("Not a User"));
        assertTrue(u1.equals(u1));
    }

    @Test
    public void testToString() {
        User u = new User(
            "u1",
            "Name",
            "pic",
            "e@mail",
            Arrays.asList("car1"),
            12345L,
            User.Gender.OTHER,
            "Loc",
            "pwd",
            "Desc",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
        String expected = "User [username=u1, role=USER, name=Name, profilePicture=pic, email=e@mail, cars=[car1], birthDate=12345, gender=OTHER, location=Loc, password=pwd, description=Desc, posts=[]]";
        assertEquals(expected, u.toString());
    }

    @Test
    public void testConstructorDeepCopy() {
        List<Post> originalPosts = new ArrayList<>();
        Post firstPost = new Post(1L, new User(), "msg", 0L, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
        originalPosts.add(firstPost);

        List<User> originalFollowers = new ArrayList<>();
        User follower = new User(
            "f1",
            "N",
            "p",
            "e@",
            new ArrayList<>(),
            BIRTH_DATE,
            User.Gender.MALE,
            "loc",
            "pw",
            "d",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
        originalFollowers.add(follower);

        List<User> originalFollowing = new ArrayList<>();
        User followingU = new User(
            "f2",
            "N",
            "p",
            "e@",
            new ArrayList<>(),
            BIRTH_DATE,
            User.Gender.FEMALE,
            "loc",
            "pw",
            "d",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
        originalFollowing.add(followingU);

        User deepCopyUser = new User(
            "copyUser",
            "X",
            "xp",
            "x@",
            new ArrayList<>(),
            BIRTH_DATE,
            User.Gender.OTHER,
            "xloc",
            "xpw",
            "xd",
            originalPosts,
            originalFollowers,
            originalFollowing
        );

        originalPosts.add(new Post());
        originalFollowers.add(new User());
        originalFollowing.add(new User());

        assertEquals(1, deepCopyUser.getPosts().size());
        assertEquals(1, deepCopyUser.getFollowers().size());
        assertEquals(1, deepCopyUser.getFollowing().size());
    }
}
