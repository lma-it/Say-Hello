package org.repository.repo;

public interface Repository<T, LId> {
    void saveEntity(T entity);
    void updateEntity(T entity);
    void deleteEntity(T entity);
    T getEntityById(LId id);
}
