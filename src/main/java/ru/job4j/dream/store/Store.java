package ru.job4j.dream.store;

import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.model.Post;
import ru.job4j.dream.model.User;

import java.util.Collection;

public interface Store {
    Collection<Post> findAllPosts();

    Collection<Candidate> findAllCandidates();

    void save(Post post);

    void save(Candidate candidate);
    void save(User user);

    Post findById(int id);

    Candidate findByCandidateId(int id);

    String getImage(int id);

    int saveImage(String name);

    void updateCandidatePhoto(int idCandidate, int idPhoto);

    void deletePhoto(int idPhoto);

    void deleteCan(int idCandidate);

    User createUser(User user);

    void updateUser(User user);

    User findByEmailUser(String email);
}
