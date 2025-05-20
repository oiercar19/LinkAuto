package com.linkauto.restapi.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        Set<Post> savedPosts = new HashSet<>();
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
            following,
            savedPosts
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
        assertFalse(user.isBanned());

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
        user.setBanned(true);

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
        assertTrue(user.isBanned());
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
    public void testSavedPostOperations() {
        assertTrue(user.getSavedPosts().isEmpty());
        Post savedPost = new Post();
        user.addSavedPost(savedPost);
        assertEquals(1, user.getSavedPosts().size());
        assertTrue(user.getSavedPosts().contains(savedPost));
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
            new ArrayList<>(),
            new HashSet<>()
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
            new ArrayList<>(),
            new HashSet<>()
        );
        user.addFollowing(followingUser);
        assertEquals(1, user.getFollowing().size());
        assertTrue(user.getFollowing().contains(followingUser));

        user.removeFollowing(followingUser);
        assertTrue(user.getFollowing().isEmpty());
    }

    @Test
    public void testEqualsComprehensive() {
        // Caso 1: Mismo objeto (reflexividad)
        assertTrue(user.equals(user), "Un objeto debe ser igual a sí mismo");

        // Caso 2: Comparación con null
        assertFalse(user.equals(null), "Un objeto no debe ser igual a null");

        // Caso 3: Comparación con diferente tipo de objeto
        assertFalse(user.equals("string"), "Un objeto no debe ser igual a otro de diferente tipo");

        // Caso 4: Dos objetos con mismo username
        User sameUsername = new User(
            "u1",  // mismo username que nuestro user de test
            "Different Name",
            "different_pic",
            "different@email.com",
            new ArrayList<>(),
            999L,
            User.Gender.MALE,
            "Different Location",
            "different_password",
            "Different description",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new HashSet<>()
        );
        assertTrue(user.equals(sameUsername), "Usuarios con mismo username deben ser iguales");

        // Caso 5: Dos objetos con username diferente
        User differentUsername = new User(
            "u2",  // username diferente
            "Name",
            "pic",
            "e@mail",
            new ArrayList<>(Arrays.asList("Toyota", "BMW")),
            BIRTH_DATE,
            User.Gender.OTHER,
            "Loc",
            "pwd",
            "Desc",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new HashSet<>()
        );

        assertFalse(user.equals(differentUsername), "Usuarios con username diferente no deben ser iguales");

        // Caso 6: Dos objetos con usernames null
        User nullUsername1 = new User();
        nullUsername1.setUsername(null);
        User nullUsername2 = new User();
        nullUsername2.setUsername(null);
        assertTrue(nullUsername1.equals(nullUsername2), "Usuarios con username null deben ser iguales entre sí");

        // Caso 7: Un objeto con username null y otro no
        User notNullUsername = new User();
        notNullUsername.setUsername("notNull");
        assertFalse(nullUsername1.equals(notNullUsername), "Usuario con username null no debe ser igual a uno con username no null");
        assertFalse(notNullUsername.equals(nullUsername1), "Usuario con username no null no debe ser igual a uno con username null");
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
            new ArrayList<>(),
            new HashSet<>()
        );
        String expected = "User [username=u1, banned=false, role=USER, name=Name, profilePicture=pic, email=e@mail, cars=[car1], birthDate=12345, gender=OTHER, location=Loc, password=pwd, description=Desc]";
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
            new ArrayList<>(),
            new HashSet<>()
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
            new ArrayList<>(),
            new HashSet<>()
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
            originalFollowing,
            new HashSet<>()
        );

        originalPosts.add(new Post());
        originalFollowers.add(new User());
        originalFollowing.add(new User());

        assertEquals(1, deepCopyUser.getPosts().size());
        assertEquals(1, deepCopyUser.getFollowers().size());
        assertEquals(1, deepCopyUser.getFollowing().size());
    }

@Test
public void testSetProfilePicture() {
    String newPic = "new_profile_picture.jpg";
    user.setProfilePicture(newPic);
    assertEquals(newPic, user.getProfilePicture());
}

@Test
public void testSetEmail() {
    String newEmail = "new@example.com";
    user.setEmail(newEmail);
    assertEquals(newEmail, user.getEmail());
}

@Test
public void testSetCars() {
    List<String> newCars = Arrays.asList("Honda", "Mercedes");
    user.setCars(newCars);
    assertEquals(newCars, user.getCars());
}

@Test
public void testSetBirthDate() {
    long newBirthDate = 789456123L;
    user.setBirthDate(newBirthDate);
    assertEquals(newBirthDate, user.getBirthDate());
}

@Test
public void testSetGender() {
    User.Gender newGender = User.Gender.FEMALE;
    user.setGender(newGender);
    assertEquals(newGender, user.getGender());
}

@Test
public void testSetLocation() {
    String newLocation = "New Location";
    user.setLocation(newLocation);
    assertEquals(newLocation, user.getLocation());
}

@Test
public void testSetPassword() {
    String newPassword = "newPassword123";
    user.setPassword(newPassword);
    assertEquals(newPassword, user.getPassword());
}

@Test
public void testSetDescription() {
    String newDescription = "This is a new description";
    user.setDescription(newDescription);
    assertEquals(newDescription, user.getDescription());
}

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
