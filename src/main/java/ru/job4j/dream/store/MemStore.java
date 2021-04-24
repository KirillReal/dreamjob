package ru.job4j.dream.store;

import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.model.Post;
import ru.job4j.dream.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MemStore implements Store {
    private static final MemStore INST = new MemStore();

    private static final AtomicInteger POST_ID = new AtomicInteger(4);
    private static final AtomicInteger CANDIDATE_ID = new AtomicInteger(4);
    private final Map<Integer, Post> posts = new ConcurrentHashMap<>();
    private final Map<Integer, Candidate> candidates = new ConcurrentHashMap<>();

    private MemStore() {
        posts.put(1, new Post(1, "Junior Java Job"));
        posts.put(2, new Post(2, "Middle Java Job"));
        posts.put(3, new Post(3, "Senior Java Job"));

    }

    public static MemStore instOf() {
        return INST;
    }

    public Collection<Post> findAllPosts() {
        return posts.values();
    }

    public Collection<Candidate> findAllCandidates() {
        return candidates.values();
    }

    public void save(Post post) {
        if (post.getId() == 0) {
            post.setId(POST_ID.incrementAndGet());
        }
        posts.put(post.getId(), post);
    }

    public void save(Candidate candidate) {
        if (candidate.getId() == 0) {
            candidate.setId(CANDIDATE_ID.incrementAndGet());
        }
        candidates.put(candidate.getId(), candidate);
    }

    @Override
    public void save(User user) {

    }

    public Post findById(int id) {
        return posts.get(id);
    }

    public Candidate findByCandidateId(int id) {
        return candidates.get(id);
    }

    @Override
    public String getImage(int id) {
        return null;
    }

    @Override
    public int saveImage(String name) {
        return 0;
    }

    @Override
    public void updateCandidatePhoto(int idCandidate, int idPhoto) {

    }

    @Override
    public void deletePhoto(int idPhoto) {

    }

    @Override
    public void deleteCan(int idCandidate) {

    }

    @Override
    public User createUser(User user) {
        return null;
    }

    @Override
    public void updateUser(User user) {

    }

    @Override
    public User findByEmailUser(String email) {
        return null;
    }

    @Override
    public int saveCity(String name) {
        return 0;
    }

    @Override
    public void updateCandidateCity(int idCandidate, int idCity) {

    }

    @Override
    public void deleteCity(int idCity) {

    }

    @Override
    public Collection<String> findAllCities() {
        return null;
    }

    @Override
    public String findByIdCity(int id) {
        return null;
    }
}
