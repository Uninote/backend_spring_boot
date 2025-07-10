package com.uninote.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.uninote.backend.config.QueryExecutionTimeInterceptor;

@NoRepositoryBean
public abstract class BaseRepository<T, ID> implements JpaRepository<T, ID> {

    @Autowired
    private QueryExecutionTimeInterceptor queryExecutionTimeInterceptor;

    protected void logQueryExecution(String queryName, long startTime, String additionalInfo) {
        long executionTime = System.currentTimeMillis() - startTime;
        queryExecutionTimeInterceptor.logQueryExecution("REPOSITORY", queryName, executionTime, additionalInfo);
    }

    protected void logQueryExecution(String queryName, long startTime) {
        logQueryExecution(queryName, startTime, "");
    }

    // Override common JpaRepository methods to add timing
    @Override
    public <S extends T> S save(S entity) {
        long startTime = System.currentTimeMillis();
        try {
            S result = getJpaRepository().save(entity);
            logQueryExecution("save", startTime, "Entity: " + entity.getClass().getSimpleName());
            return result;
        } catch (Exception e) {
            logQueryExecution("save", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        long startTime = System.currentTimeMillis();
        try {
            List<S> result = getJpaRepository().saveAll(entities);
            logQueryExecution("saveAll", startTime, "Count: " + result.size());
            return result;
        } catch (Exception e) {
            logQueryExecution("saveAll", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        long startTime = System.currentTimeMillis();
        try {
            Optional<T> result = getJpaRepository().findById(id);
            logQueryExecution("findById", startTime, "ID: " + id);
            return result;
        } catch (Exception e) {
            logQueryExecution("findById", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean existsById(ID id) {
        long startTime = System.currentTimeMillis();
        try {
            boolean result = getJpaRepository().existsById(id);
            logQueryExecution("existsById", startTime, "ID: " + id);
            return result;
        } catch (Exception e) {
            logQueryExecution("existsById", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<T> findAll() {
        long startTime = System.currentTimeMillis();
        try {
            List<T> result = getJpaRepository().findAll();
            logQueryExecution("findAll", startTime, "Count: " + result.size());
            return result;
        } catch (Exception e) {
            logQueryExecution("findAll", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<T> findAllById(Iterable<ID> ids) {
        long startTime = System.currentTimeMillis();
        try {
            List<T> result = getJpaRepository().findAllById(ids);
            logQueryExecution("findAllById", startTime, "Count: " + result.size());
            return result;
        } catch (Exception e) {
            logQueryExecution("findAllById", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public long count() {
        long startTime = System.currentTimeMillis();
        try {
            long result = getJpaRepository().count();
            logQueryExecution("count", startTime, "Result: " + result);
            return result;
        } catch (Exception e) {
            logQueryExecution("count", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteById(ID id) {
        long startTime = System.currentTimeMillis();
        try {
            getJpaRepository().deleteById(id);
            logQueryExecution("deleteById", startTime, "ID: " + id);
        } catch (Exception e) {
            logQueryExecution("deleteById", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(T entity) {
        long startTime = System.currentTimeMillis();
        try {
            getJpaRepository().delete(entity);
            logQueryExecution("delete", startTime, "Entity: " + entity.getClass().getSimpleName());
        } catch (Exception e) {
            logQueryExecution("delete", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        long startTime = System.currentTimeMillis();
        try {
            getJpaRepository().deleteAllById(ids);
            logQueryExecution("deleteAllById", startTime, "Count: " + ((List<?>) ids).size());
        } catch (Exception e) {
            logQueryExecution("deleteAllById", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        long startTime = System.currentTimeMillis();
        try {
            getJpaRepository().deleteAll(entities);
            logQueryExecution("deleteAll", startTime, "Count: " + ((List<?>) entities).size());
        } catch (Exception e) {
            logQueryExecution("deleteAll", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteAll() {
        long startTime = System.currentTimeMillis();
        try {
            getJpaRepository().deleteAll();
            logQueryExecution("deleteAll", startTime, "All entities");
        } catch (Exception e) {
            logQueryExecution("deleteAll", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void flush() {
        long startTime = System.currentTimeMillis();
        try {
            getJpaRepository().flush();
            logQueryExecution("flush", startTime);
        } catch (Exception e) {
            logQueryExecution("flush", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public <S extends T> S saveAndFlush(S entity) {
        long startTime = System.currentTimeMillis();
        try {
            S result = getJpaRepository().saveAndFlush(entity);
            logQueryExecution("saveAndFlush", startTime, "Entity: " + entity.getClass().getSimpleName());
            return result;
        } catch (Exception e) {
            logQueryExecution("saveAndFlush", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public <S extends T> List<S> saveAllAndFlush(Iterable<S> entities) {
        long startTime = System.currentTimeMillis();
        try {
            List<S> result = getJpaRepository().saveAllAndFlush(entities);
            logQueryExecution("saveAllAndFlush", startTime, "Count: " + result.size());
            return result;
        } catch (Exception e) {
            logQueryExecution("saveAllAndFlush", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteAllInBatch(Iterable<T> entities) {
        long startTime = System.currentTimeMillis();
        try {
            getJpaRepository().deleteAllInBatch(entities);
            logQueryExecution("deleteAllInBatch", startTime, "Count: " + ((List<?>) entities).size());
        } catch (Exception e) {
            logQueryExecution("deleteAllInBatch", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<ID> ids) {
        long startTime = System.currentTimeMillis();
        try {
            getJpaRepository().deleteAllByIdInBatch(ids);
            logQueryExecution("deleteAllByIdInBatch", startTime, "Count: " + ((List<?>) ids).size());
        } catch (Exception e) {
            logQueryExecution("deleteAllByIdInBatch", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteAllInBatch() {
        long startTime = System.currentTimeMillis();
        try {
            getJpaRepository().deleteAllInBatch();
            logQueryExecution("deleteAllInBatch", startTime, "All entities");
        } catch (Exception e) {
            logQueryExecution("deleteAllInBatch", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public T getOne(ID id) {
        long startTime = System.currentTimeMillis();
        try {
            T result = getJpaRepository().getOne(id);
            logQueryExecution("getOne", startTime, "ID: " + id);
            return result;
        } catch (Exception e) {
            logQueryExecution("getOne", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public T getById(ID id) {
        long startTime = System.currentTimeMillis();
        try {
            T result = getJpaRepository().getById(id);
            logQueryExecution("getById", startTime, "ID: " + id);
            return result;
        } catch (Exception e) {
            logQueryExecution("getById", startTime, "ERROR: " + e.getMessage());
            throw e;
        }
    }



    // Abstract method to get the actual JpaRepository implementation
    protected abstract JpaRepository<T, ID> getJpaRepository();
} 