/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.module.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.context.module.ApplicationName;
import com.navercorp.pinpoint.profiler.context.module.BootstrapJarPaths;
import com.navercorp.pinpoint.profiler.context.module.PluginJars;
import com.navercorp.pinpoint.profiler.context.provider.AgentStartTimeProvider;
import com.navercorp.pinpoint.profiler.context.provider.InterceptorRegistryBinderProvider;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;

import java.lang.instrument.Instrumentation;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ConfigModule extends AbstractModule {

    private final AgentOption agentOption;

    public ConfigModule(AgentOption agentOption) {
        this.agentOption = Assert.requireNonNull(agentOption, "profilerConfig must not be null");
        Assert.requireNonNull(agentOption.getProfilerConfig(), "profilerConfig must not be null");


    }

    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        binder().requireAtInjectOnConstructors();
        binder().disableCircularProxies();

        ProfilerConfig profilerConfig = agentOption.getProfilerConfig();

        bind(ProfilerConfig.class).toInstance(profilerConfig);

        bindConstants(profilerConfig);


        bind(Instrumentation.class).toInstance(agentOption.getInstrumentation());

        bind(InterceptorRegistryBinder.class).toProvider(InterceptorRegistryBinderProvider.class).in(Scopes.SINGLETON);

        TypeLiteral<List<String>> pluginJarFile = new TypeLiteral<List<String>>() {};
        bind(pluginJarFile).annotatedWith(PluginJars.class).toInstance(agentOption.getPluginJars());

        TypeLiteral<List<String>> bootstrapJarFIle = new TypeLiteral<List<String>>() {};
        bind(bootstrapJarFIle).annotatedWith(BootstrapJarPaths.class).toInstance(agentOption.getBootstrapJarPaths());

        bindAgentInformation(agentOption.getAgentId(), agentOption.getApplicationName());
    }

    private void bindConstants(ProfilerConfig profilerConfig) {
        bindConstant().annotatedWith(TraceAgentActiveThread.class).to(profilerConfig.isTraceAgentActiveThread());

        bindConstant().annotatedWith(DeadlockMonitorEnable.class).to(profilerConfig.isDeadlockMonitorEnable());
        bindConstant().annotatedWith(DeadlockMonitorInterval.class).to(profilerConfig.getDeadlockMonitorInterval());
    }

    private void bindAgentInformation(String agentId, String applicationName) {

        bind(String.class).annotatedWith(AgentId.class).toInstance(agentId);
        bind(String.class).annotatedWith(ApplicationName.class).toInstance(applicationName);
        bind(Long.class).annotatedWith(AgentStartTime.class).toProvider(AgentStartTimeProvider.class).in(Scopes.SINGLETON);
    }
}