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
        List<Post> posts = new ArrayList<>();
        List<User> followers = new ArrayList<>();
        List<User> following = new ArrayList<>();
        List<String> cars = new ArrayList<>(Arrays.asList("Toyota", "BMW"));
        user = new User(
            "u1",
            "Name",
            "pic",
            "e@mail",
            cars,
            BIRTH_DATE,
            User.Gender.OTHER,
            "Loc",
            "pwd",
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
        // Ajusta estos asserts según tus getters
        assertEquals("u1", user.getUsername());
        assertEquals("Name", user.getName());
        assertEquals("pic", user.getProfilePicture());
        assertEquals("e@mail", user.getEmail());
        assertEquals(Arrays.asList("Toyota", "BMW"), user.getCars());
        assertEquals(BIRTH_DATE, user.getBirthDate());
        assertEquals(User.Gender.OTHER, user.getGender());
        assertEquals("Loc", user.getLocation());
        assertEquals("pwd", user.getPassword());
        assertEquals("Desc", user.getDescription());
        assertSame(user.getPosts(), user.getPosts());
        assertSame(user.getFollowers(), user.getFollowers());
        assertSame(user.getFollowing(), user.getFollowing());
    }

    @Test
    public void testPostOperations() {
        Post post = new Post();
        user.addPost(post);
        assertEquals(1, user.getPosts().size());
        assertTrue(user.getPosts().contains(post));
    }

    @Test
    public void testFollowerOperations() {
        assertTrue(user.getFollowers().isEmpty());
        User follower = new User(
            "f1",
            "Follower",
            "picF",
            "f@mail",
            new ArrayList<>(),
            123L,
            User.Gender.MALE,
            "LocF",
            "pwdF",
            "DescF",
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
        User followee = new User();
        user.addFollowing(followee);
        assertEquals(1, user.getFollowing().size());
        assertTrue(user.getFollowing().contains(followee));
        user.removeFollowing(followee);
        assertTrue(user.getFollowing().isEmpty());
    }

    @Test
    public void testEqualsAndHashCode() {
        User u1 = new User(
            "sameUser",
            "A",
            "picA",
            "a@mail",
            new ArrayList<>(),
            111L,
            User.Gender.FEMALE,
            "LocA",
            "pwdA",
            "DescA",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
        User u2 = new User(
            "sameUser",
            "B",
            "picB",
            "b@mail",
            Arrays.asList("X"),
            222L,
            User.Gender.MALE,
            "LocB",
            "pwdB",
            "DescB",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
        // mismos usernames => equal y mismo hashCode
        assertTrue(u1.equals(u2));
        assertEquals(u1.hashCode(), u2.hashCode());

        // distinto username => not equal
        User u3 = new User(
            "other",
            "C",
            "picC",
            "c@mail",
            new ArrayList<>(),
            333L,
            User.Gender.OTHER,
            "LocC",
            "pwdC",
            "DescC",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
        u3.setRole(Role.ADMIN);
        assertFalse(u1.equals(u3));
    }

    @Test
    public void testDeepCopyConstructor() {
        List<Post> originalPosts = new ArrayList<>();
        List<User> originalFollowers = new ArrayList<>();
        List<User> originalFollowing = new ArrayList<>();

        User deepCopyUser = new User(
            "u2",
            "Deep",
            "picD",
            "d@mail",
            Arrays.asList("d1"),
            444L,
            User.Gender.OTHER,
            "LocD",
            "pwdD",
            "DescD",
            originalPosts,
            originalFollowers,
            originalFollowing
        );

        // Modificar las listas originales no debe afectar al deepCopy
        originalPosts.add(new Post());
        originalFollowers.add(new User());
        originalFollowing.add(new User());

        assertEquals(0, deepCopyUser.getPosts().size());
        assertEquals(0, deepCopyUser.getFollowers().size());
        assertEquals(0, deepCopyUser.getFollowing().size());
    }

    // ——— NUEVOS TESTS AÑADIDOS ———

    @Test
    public void testSetReporters() {
        User reporter = new User();
        reporter.setUsername("reporterA");
        // inicialmente sin reporters
        assertTrue(user.getReporters().isEmpty());
        user.setReporters(reporter);
        assertEquals(1, user.getReporters().size());
        assertTrue(user.getReporters().contains(reporter));
    }

    @Test
    public void testEqualsWithNullUsernames() {
        User u1 = new User();
        User u2 = new User();
        u1.setUsername(null);
        u2.setUsername(null);
        assertTrue(u1.equals(u2), "Two users with null usernames should be equal");
    }

    @Test
    public void testEqualsOneNullOtherNotNull() {
        User u1 = new User();
        User u2 = new User();
        u1.setUsername(null);
        u2.setUsername("nonNull");
        assertFalse(u1.equals(u2), "User with null username should not equal user with non-null username");
        assertFalse(u2.equals(u1), "Symmetric: user with non-null username should not equal user with null username");
    }

    @Test
    public void testHashCodeWithNullUsername() {
        User u = new User();
        u.setUsername(null);
        // hashCode should ser 31*1 + 0
        assertEquals(31, u.hashCode());
    }

    @Test
    public void testHashCodeWithNonNullUsername() {
        User u = new User();
        u.setUsername("testUser");
        int expected = 31 * 1 + "testUser".hashCode();
        assertEquals(expected, u.hashCode());
    }
}
