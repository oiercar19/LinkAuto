package com.linkauto.restapi.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
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
            new ArrayList<>()
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
            new ArrayList<>()
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
    public void testDeepCopyConstructor() {
        // Crear listas con elementos reales para probar los bucles for en el constructor
        List<Post> originalPosts = new ArrayList<>();
        Post post1 = new Post();
        originalPosts.add(post1);

        List<User> originalFollowers = new ArrayList<>();
        User follower1 = new User();
        follower1.setUsername("follower1");
        originalFollowers.add(follower1);

        List<User> originalFollowing = new ArrayList<>();
        User following1 = new User();
        following1.setUsername("following1");
        originalFollowing.add(following1);

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

        // Verificar que las listas se copiaron correctamente
        assertEquals(1, deepCopyUser.getPosts().size());
        assertEquals(post1.getId(), deepCopyUser.getPosts().get(0).getId());
        assertEquals(1, deepCopyUser.getFollowers().size());
        assertEquals("follower1", deepCopyUser.getFollowers().get(0).getUsername());
        assertEquals(1, deepCopyUser.getFollowing().size());
        assertEquals("following1", deepCopyUser.getFollowing().get(0).getUsername());

        // Modificar las listas originales no debe afectar al deepCopy
        originalPosts.add(new Post());
        originalFollowers.add(new User());
        originalFollowing.add(new User());

        // Las listas del usuario no deben haberse modificado
        assertEquals(1, deepCopyUser.getPosts().size());
        assertEquals(1, deepCopyUser.getFollowers().size());
        assertEquals(1, deepCopyUser.getFollowing().size());
    }
    public void testSetName() {
        String newName = "New Name";
        user.setName(newName);
        assertEquals(newName, user.getName());
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
