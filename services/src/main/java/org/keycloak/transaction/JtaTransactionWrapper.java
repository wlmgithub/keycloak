/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.transaction;

import org.keycloak.models.KeycloakTransaction;

import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class JtaTransactionWrapper implements KeycloakTransaction {
    protected TransactionManager tm;
    protected Transaction ut;
    protected Transaction suspended;

    public JtaTransactionWrapper(TransactionManager tm) {
        this.tm = tm;
        try {
            suspended = tm.suspend();
            tm.begin();
            ut = tm.getTransaction();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void begin() {
    }

    @Override
    public void commit() {
        try {
            ut.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rollback() {
        try {
            ut.rollback();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            end();
        }

    }

    @Override
    public void setRollbackOnly() {
        try {
            ut.setRollbackOnly();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean getRollbackOnly() {
        try {
            return ut.getStatus() == Status.STATUS_MARKED_ROLLBACK;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isActive() {
        try {
            return ut.getStatus() == Status.STATUS_ACTIVE;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void end() {
        if (suspended != null) {
            try {
                tm.resume(suspended);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}