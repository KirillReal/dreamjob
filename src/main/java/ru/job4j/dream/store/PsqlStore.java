package ru.job4j.dream.store;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.model.Post;
import ru.job4j.dream.model.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {
    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class.getName());
    private final BasicDataSource pool = new BasicDataSource();

    private PsqlStore() {
        Properties cfg = new Properties();
        try (BufferedReader io = new BufferedReader(
                new FileReader("db.properties")
        )) {
            cfg.load(io);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        pool.setDriverClassName(cfg.getProperty("jdbc.driver"));
        pool.setUrl(cfg.getProperty("jdbc.url"));
        pool.setUsername(cfg.getProperty("jdbc.username"));
        pool.setPassword(cfg.getProperty("jdbc.password"));
        pool.setMinIdle(5);
        pool.setMaxIdle(10);
        pool.setMaxOpenPreparedStatements(100);
    }

    private static final class Lazy {
        private static final Store INST = new PsqlStore();
    }

    public static Store instOf() {
        return Lazy.INST;
    }

    @Override
    public Collection<Post> findAllPosts() {
        List<Post> posts = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("SELECT * FROM post")
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    posts.add(new Post(it.getInt("id"), it.getString("name")));
                }
            }
        } catch (Exception e) {
            LOG.error("Error", e);
        }
        return posts;
    }

    @Override
    public Collection<Candidate> findAllCandidates() {
        List<Candidate> candidates = new ArrayList<>();
        try (Connection cn = pool.getConnection();
        PreparedStatement ps = cn.prepareStatement("SELECT * FROM candidate")
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    candidates.add(new Candidate(it.getInt("id"), it.getString("name"),
                            it.getInt(3), it.getInt(4)));
                }
            }
        } catch (Exception e) {
            LOG.error("Error", e);
        }
        return candidates;
    }

    @Override
    public void save(Post post) {
        if (post.getId() == 0) {
            create(post);
        } else {
            update(post);
        }
    }

    private Post create(Post post) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("INSERT INTO post(name) VALUES (?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, post.getName());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    post.setId(id.getInt(1));
                }
            }
        } catch (Exception trow) {
            LOG.error("There was an error creating", trow);
        }
        return post;
    }

    private void update(Post post) {
        try (Connection cn = pool.getConnection();
        PreparedStatement ps = cn.prepareStatement("UPDATE post set name= (?) where id= (?) ")) {
            ps.setString(1, post.getName());
            ps.setInt(2, post.getId());
            ps.executeUpdate();
        } catch (SQLException throwable) {
            LOG.error("Update error", throwable);
        }
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM post where id=?")) {
            ps.setInt(1, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    post = new Post(resultSet.getInt(1),
                            resultSet.getString(2));
                }
            }
        } catch (SQLException throwable) {
            LOG.error("No post with this id was found", throwable);
        }
        return post;
    }

    @Override
    public void save(Candidate candidate) {
        if (candidate.getId() == 0) {
            create(candidate);
        } else {
            update(candidate);
        }
    }

    private Candidate create(Candidate candidate) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO candidate(name,city_id) VALUES (?,?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, candidate.getName());
            ps.setInt(2, candidate.getCityId());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    candidate.setId(id.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.error("There was an error creating", e);
        }
        return candidate;
    }

    @Override
    public void save(User user) {
        if (user.getId() == 0) {
            createUser(user);
        } else {
            updateUser(user);
        }
    }

    private void update(Candidate candidate) {
        try (Connection cn = pool.getConnection();
             PreparedStatement statement = cn.prepareStatement(
                     "UPDATE candidate set name = (?), city_id = (?)  where id=? ")) {
            statement.setString(1, candidate.getName());
            statement.setInt(2, candidate.getCityId());
            statement.setInt(3, candidate.getId());
            statement.executeUpdate();
        } catch (SQLException throwable) {
            LOG.error("Update error", throwable);
        }
    }

    @Override
    public Candidate findByCandidateId(int id) {
        Candidate candidate = null;
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM candidate where id= (?)")) {
            ps.setInt(1, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    candidate = new Candidate(resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getInt(3),
                            resultSet.getInt(4));
                }
            }
        } catch (SQLException throwable) {
            LOG.error("No candidate with this id was found", throwable);
        }
        return candidate;
    }

    @Override
    public String getImage(int id) {
        String result = "";
        try (Connection cn = pool.getConnection();
             PreparedStatement statement = cn
                     .prepareStatement("SELECT * FROM photos WHERE id = (?)")
        ) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    result = resultSet.getString(2);
                }
            }
        } catch (Exception e) {
            LOG.error("db ex", e);
        }
        return result;
    }

    @Override
    public int saveImage(String name) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("INSERT INTO photos (name) VALUES (?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    return id.getInt(1);
                }
            }
        } catch (SQLException throwables) {
            LOG.error("db ex", throwables);
        }
        return -1;
    }

    @Override
    public void updateCandidatePhoto(int idCandidate, int idPhoto) {
        try (Connection cn = pool.getConnection();
             PreparedStatement statement = cn.prepareStatement(
                     "UPDATE candidate set photoId= (?) where id= (?)")) {
            statement.setInt(1, idPhoto);
            statement.setInt(2, idCandidate);
            statement.executeUpdate();
        } catch (SQLException throwable) {
            LOG.error("db ex", throwable);
        }
    }

    @Override
    public void deletePhoto(int idPhoto) {
        try (Connection cn = pool.getConnection();
             PreparedStatement statement = cn.prepareStatement(
                     "DELETE FROM photos where id= (?)")) {
            statement.setInt(1, idPhoto);
            statement.executeUpdate();
        } catch (SQLException throwable) {
            LOG.error("db ex", throwable);
        }
    }

    @Override
    public void deleteCan(int idCandidate) {
        try (Connection cn = pool.getConnection();
             PreparedStatement statement = cn.prepareStatement(
                     "DELETE FROM candidate where id= (?)")) {
            statement.setInt(1, idCandidate);
            statement.executeUpdate();
        } catch (SQLException throwable) {
            LOG.error("db ex", throwable);
        }
    }

    @Override
    public User createUser(User user) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO users" + "(name,email,password) VALUES (?, ?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    user.setId(id.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.error("There was an error creating", e);
        }
        return user;
    }

    @Override
    public void updateUser(User user) {
        try (Connection cn = pool.getConnection();
             PreparedStatement statement = cn.prepareStatement(
                     "UPDATE users set name= (?), email= (?), password= (?)"
                             + " where id= (?) ")) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setInt(4, user.getId());
            statement.executeUpdate();
        } catch (SQLException throwable) {
            LOG.error("Update error", throwable);
        }
    }

    @Override
    public User findByEmailUser(String email) {
        User user = null;
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM users where email = (?)",
             PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, email);
            try (ResultSet it = ps.executeQuery()) {
                if (it.next()) {
                    user = new User(Integer.parseInt(it.getString("id")),
                            it.getString("name"),
                            it.getString("email"),
                            it.getString("password"));
                }
            }
        } catch (Exception e) {
            LOG.error("Error", e);
        }
        return user;
    }

    @Override
    public int saveCity(String nameCity) {
        int id = -1;
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("INSERT INTO city (name) VALUES (?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nameCity);
            ps.execute();
            ResultSet resultSet = ps.getGeneratedKeys();
                if (resultSet.next()) {
                    id = resultSet.getInt("city_id");
                }

        } catch (SQLException throwables) {
            LOG.error("db ex", throwables);
        }
        return id;
    }

    @Override
    public void updateCandidateCity(int idCandidate, int idCity) {
        try (Connection cn = pool.getConnection();
             PreparedStatement statement = cn.prepareStatement(
                     "UPDATE candidate set city_id= (?) where id= (?) ")) {
            statement.setInt(1, idCity);
            statement.setInt(2, idCandidate);
            statement.executeUpdate();
        } catch (SQLException throwable) {
            LOG.error("Update error", throwable);
        }
    }

    @Override
    public void deleteCity(int idCity) {
        try (Connection cn = pool.getConnection();
             PreparedStatement statement = cn.prepareStatement(
                     "DELETE FROM city where id= (?)")) {
            statement.setInt(1, idCity);
            statement.executeUpdate();
        } catch (SQLException throwable) {
            LOG.error("db ex", throwable);
        }
    }

    @Override
    public Collection<String> findAllCities() {
        List<String> cities = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM city")
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    cities.add(it.getString("name"));
                }
            }
        } catch (Exception e) {
            LOG.error("Error", e);
        }
        return cities;
    }

    @Override
    public String findByIdCity(int id) {
        String city = "";
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM city where id= (?)")) {
            ps.setInt(1, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                   city = resultSet.getString(2);
                }
            }
        } catch (SQLException throwable) {
            LOG.error("No candidate with this id was found", throwable);
        }
        return city;
    }
}
