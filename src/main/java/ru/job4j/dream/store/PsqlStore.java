package ru.job4j.dream.store;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.model.Post;

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
            e.printStackTrace();
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
                    candidates.add(new Candidate(it.getInt("id"),it.getString("name")));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
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
             PreparedStatement ps =  cn.prepareStatement("INSERT INTO post(name) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, post.getName());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    post.setId(id.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    private void update(Post post) {
        try (Connection cn = pool.getConnection();
        PreparedStatement ps = cn.prepareStatement("UPDATE post set name=? where id=? ")) {
            ps.setString(1,post.getName());
            ps.setInt(2,post.getId());
            ps.executeUpdate();
        }catch (SQLException throwable) {
            LOG.error("db exception",throwable);
        }
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (Connection cn = pool.getConnection();
        PreparedStatement ps = cn.prepareStatement("SELECT FROM post where id=?")) {
            ps.setInt(1,id);
            try (ResultSet resultSet = ps.executeQuery()) {
                if(resultSet.next()) {
                    post = new Post(resultSet.getInt(1),
                            resultSet.getString(2));
                }
            }
        }catch (SQLException throwable) {
            LOG.error("db exception",throwable);
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
                     "INSERT INTO candidate(name, cityId) VALUES (?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, candidate.getName());
            ps.setInt(2, candidate.getId());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    candidate.setId(id.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.error("db ex", e);
        }
        return candidate;
    }

    private void update(Candidate candidate) {
        try (Connection cn = pool.getConnection();
             PreparedStatement statement = cn.prepareStatement(
                     "UPDATE candidate set name = (?), cityId = (?) where id= (?) ")) {
            statement.setString(1, candidate.getName());
            statement.setInt(2, candidate.getId());
            statement.executeUpdate();
        } catch (SQLException throwable) {
            LOG.error("db ex", throwable);
        }
    }

    @Override
    public Candidate findByCandidateId (int id) {
        Candidate candidate = null;
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT FROM post where id=?")) {
            ps.setInt(1,id);
            try (ResultSet resultSet = ps.executeQuery()) {
                if(resultSet.next()) {
                    candidate = new Candidate(resultSet.getInt(1),
                            resultSet.getString(2));
                }
            }
        }catch (SQLException throwable) {
            LOG.error("db exception",throwable);
        }
        return candidate;
    }
}
