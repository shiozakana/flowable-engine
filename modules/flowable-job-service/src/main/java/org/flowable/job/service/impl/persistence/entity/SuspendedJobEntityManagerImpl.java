/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flowable.job.service.impl.persistence.entity;

import java.util.List;

import org.flowable.engine.common.api.delegate.event.FlowableEngineEventType;
import org.flowable.job.api.Job;
import org.flowable.job.service.JobServiceConfiguration;
import org.flowable.job.service.event.impl.FlowableJobEventBuilder;
import org.flowable.job.service.impl.SuspendedJobQueryImpl;
import org.flowable.job.service.impl.persistence.entity.data.SuspendedJobDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tijs Rademakers
 */
public class SuspendedJobEntityManagerImpl extends AbstractEntityManager<SuspendedJobEntity> implements SuspendedJobEntityManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuspendedJobEntityManagerImpl.class);

    protected SuspendedJobDataManager jobDataManager;

    public SuspendedJobEntityManagerImpl(JobServiceConfiguration jobServiceConfiguration, SuspendedJobDataManager jobDataManager) {
        super(jobServiceConfiguration);
        this.jobDataManager = jobDataManager;
    }

    @Override
    public List<SuspendedJobEntity> findJobsByExecutionId(String id) {
        return jobDataManager.findJobsByExecutionId(id);
    }

    @Override
    public List<SuspendedJobEntity> findJobsByProcessInstanceId(String id) {
        return jobDataManager.findJobsByProcessInstanceId(id);
    }

    @Override
    public List<Job> findJobsByQueryCriteria(SuspendedJobQueryImpl jobQuery) {
        return jobDataManager.findJobsByQueryCriteria(jobQuery);
    }

    @Override
    public long findJobCountByQueryCriteria(SuspendedJobQueryImpl jobQuery) {
        return jobDataManager.findJobCountByQueryCriteria(jobQuery);
    }

    @Override
    public void updateJobTenantIdForDeployment(String deploymentId, String newTenantId) {
        jobDataManager.updateJobTenantIdForDeployment(deploymentId, newTenantId);
    }

    @Override
    public void insert(SuspendedJobEntity jobEntity, boolean fireCreateEvent) {
        getJobServiceConfiguration().getInternalJobManager().handleJobInsert(jobEntity);

        jobEntity.setCreateTime(getJobServiceConfiguration().getClock().getCurrentTime());
        super.insert(jobEntity, fireCreateEvent);
    }

    @Override
    public void insert(SuspendedJobEntity jobEntity) {
        insert(jobEntity, true);
    }

    @Override
    public void delete(SuspendedJobEntity jobEntity) {
        super.delete(jobEntity);

        deleteExceptionByteArrayRef(jobEntity);

        getJobServiceConfiguration().getInternalJobManager().handleJobDelete(jobEntity);

        // Send event
        if (getEventDispatcher().isEnabled()) {
            getEventDispatcher().dispatchEvent(FlowableJobEventBuilder.createEntityEvent(FlowableEngineEventType.ENTITY_DELETED, this));
        }
    }

    /**
     * Deletes a the byte array used to store the exception information. Subclasses may override to provide custom implementations.
     */
    protected void deleteExceptionByteArrayRef(SuspendedJobEntity jobEntity) {
        JobByteArrayRef exceptionByteArrayRef = jobEntity.getExceptionByteArrayRef();
        if (exceptionByteArrayRef != null) {
            exceptionByteArrayRef.delete();
        }
    }

    protected SuspendedJobEntity createSuspendedJob(AbstractRuntimeJobEntity job) {
        SuspendedJobEntity newSuspendedJobEntity = create();
        newSuspendedJobEntity.setJobHandlerConfiguration(job.getJobHandlerConfiguration());
        newSuspendedJobEntity.setJobHandlerType(job.getJobHandlerType());
        newSuspendedJobEntity.setExclusive(job.isExclusive());
        newSuspendedJobEntity.setRepeat(job.getRepeat());
        newSuspendedJobEntity.setRetries(job.getRetries());
        newSuspendedJobEntity.setEndDate(job.getEndDate());
        newSuspendedJobEntity.setExecutionId(job.getExecutionId());
        newSuspendedJobEntity.setProcessInstanceId(job.getProcessInstanceId());
        newSuspendedJobEntity.setProcessDefinitionId(job.getProcessDefinitionId());

        // Inherit tenant
        newSuspendedJobEntity.setTenantId(job.getTenantId());
        newSuspendedJobEntity.setJobType(job.getJobType());
        return newSuspendedJobEntity;
    }

    @Override
    protected SuspendedJobDataManager getDataManager() {
        return jobDataManager;
    }

    public void setJobDataManager(SuspendedJobDataManager jobDataManager) {
        this.jobDataManager = jobDataManager;
    }
}
