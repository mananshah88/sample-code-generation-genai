package com.example.fileuploadservice.dao;

import com.example.fileuploadservice.model.FileStorage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import java.util.function.Function;

/**
 * Immutable wrapper for FileStorageRepository to prevent modification of internal state
 */
public final class ImmutableFileStorageRepository implements FileStorageRepository {
    private final FileStorageRepository delegate;

    public ImmutableFileStorageRepository(FileStorageRepository delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("FileStorageRepository delegate cannot be null");
        }
        this.delegate = delegate;
    }

    @Override
    public List<FileStorage> findByOrderId(String orderId) {
        return delegate.findByOrderId(orderId);
    }

    @Override
    public Optional<FileStorage> findByOrderIdAndFileName(String orderId, String fileName) {
        return delegate.findByOrderIdAndFileName(orderId, fileName);
    }

    @Override
    public <S extends FileStorage> S save(S entity) {
        return delegate.save(entity);
    }

    @Override
    public void delete(FileStorage entity) {
        delegate.delete(entity);
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    @Override
    public <S extends FileStorage> S saveAndFlush(S entity) {
        return delegate.saveAndFlush(entity);
    }

    @Override
    public <S extends FileStorage> List<S> saveAllAndFlush(Iterable<S> entities) {
        return delegate.saveAllAndFlush(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<FileStorage> entities) {
        delegate.deleteAllInBatch(entities);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        delegate.deleteAllByIdInBatch(ids);
    }

    @Override
    public void deleteAllInBatch() {
        delegate.deleteAllInBatch();
    }

    @Override
    public FileStorage getOne(Long id) {
        return delegate.getOne(id);
    }

    @Override
    public FileStorage getById(Long id) {
        return delegate.getById(id);
    }

    @Override
    public FileStorage getReferenceById(Long id) {
        return delegate.getReferenceById(id);
    }

    @Override
    public <S extends FileStorage> List<S> findAll(Example<S> example) {
        return delegate.findAll(example);
    }

    @Override
    public <S extends FileStorage> List<S> findAll(Example<S> example, Sort sort) {
        return delegate.findAll(example, sort);
    }

    @Override
    public <S extends FileStorage> List<S> saveAll(Iterable<S> entities) {
        return delegate.saveAll(entities);
    }

    @Override
    public List<FileStorage> findAll() {
        return delegate.findAll();
    }

    @Override
    public List<FileStorage> findAllById(Iterable<Long> ids) {
        return delegate.findAllById(ids);
    }

    @Override
    public Optional<FileStorage> findById(Long id) {
        return delegate.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return delegate.existsById(id);
    }

    @Override
    public long count() {
        return delegate.count();
    }

    @Override
    public void deleteById(Long id) {
        delegate.deleteById(id);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        delegate.deleteAllById(ids);
    }

    @Override
    public void deleteAll(Iterable<? extends FileStorage> entities) {
        delegate.deleteAll(entities);
    }

    @Override
    public void deleteAll() {
        delegate.deleteAll();
    }

    @Override
    public List<FileStorage> findAll(Sort sort) {
        return delegate.findAll(sort);
    }

    @Override
    public Page<FileStorage> findAll(Pageable pageable) {
        return delegate.findAll(pageable);
    }

    @Override
    public <S extends FileStorage> Optional<S> findOne(Example<S> example) {
        return delegate.findOne(example);
    }

    @Override
    public <S extends FileStorage> Page<S> findAll(Example<S> example, Pageable pageable) {
        return delegate.findAll(example, pageable);
    }

    @Override
    public <S extends FileStorage> long count(Example<S> example) {
        return delegate.count(example);
    }

    @Override
    public <S extends FileStorage> boolean exists(Example<S> example) {
        return delegate.exists(example);
    }

    @Override
    public <S extends FileStorage, R> R findBy(Example<S> example,
            Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return delegate.findBy(example, queryFunction);
    }
}
