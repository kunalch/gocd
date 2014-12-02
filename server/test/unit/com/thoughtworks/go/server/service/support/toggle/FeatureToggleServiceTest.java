/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/

package com.thoughtworks.go.server.service.support.toggle;

import com.thoughtworks.go.server.domain.support.toggle.FeatureToggle;
import com.thoughtworks.go.server.domain.support.toggle.FeatureToggles;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FeatureToggleServiceTest {
    @Mock
    public FeatureToggleRepository repository;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldListAllFeatureToggles() throws Exception {
        FeatureToggles existingToggles = new FeatureToggles(
                new FeatureToggle("key1", "description1", true),
                new FeatureToggle("key2", "description2", false)
        );

        when(repository.availableToggles()).thenReturn(existingToggles);
        when(repository.userToggles()).thenReturn(new FeatureToggles());

        FeatureToggleService service = new FeatureToggleService(repository);

        assertThat(service.allToggles(), is(existingToggles));
    }

    @Test
    public void shouldKnowWhetherAToggleIsOnOrOff() throws Exception {
        FeatureToggles existingToggles = new FeatureToggles(
                new FeatureToggle("key1", "description1", true),
                new FeatureToggle("key2", "description2", false)
        );

        when(repository.availableToggles()).thenReturn(existingToggles);
        when(repository.userToggles()).thenReturn(new FeatureToggles());

        FeatureToggleService service = new FeatureToggleService(repository);

        assertThat(service.isToggleOn("key1"), is(true));
        assertThat(service.isToggleOn("key2"), is(false));
    }

    @Test
    public void shouldSayThatNonExistentTogglesAreOff() throws Exception {
        FeatureToggles existingToggles = new FeatureToggles(
                new FeatureToggle("key1", "description1", true),
                new FeatureToggle("key2", "description2", false)
        );

        when(repository.availableToggles()).thenReturn(existingToggles);
        when(repository.userToggles()).thenReturn(new FeatureToggles());

        FeatureToggleService service = new FeatureToggleService(repository);

        assertThat(service.isToggleOn("NON_EXISTENT_KEY"), is(false));
    }

    @Test
    public void shouldOverrideAvailableToggleValuesWithValuesFromUsersToggles() throws Exception {
        FeatureToggle availableToggle1 = new FeatureToggle("key1", "desc1", true);
        FeatureToggle availableToggle2 = new FeatureToggle("key2", "desc2", true);
        FeatureToggle availableToggle3 = new FeatureToggle("key3", "desc3", true);
        when(repository.availableToggles()).thenReturn(new FeatureToggles(availableToggle1, availableToggle2, availableToggle3));

        FeatureToggle userToggle1 = new FeatureToggle("key1", "NEW_desc1_WITH_NO_change_to_value", true);
        FeatureToggle userToggle2 = new FeatureToggle("key2", "NEW_desc2_WITH_CHANGE_TO_VALUE", false);
        when(repository.userToggles()).thenReturn(new FeatureToggles(userToggle1, userToggle2));

        FeatureToggleService service = new FeatureToggleService(repository);
        FeatureToggles toggles = service.allToggles();

        assertThat(toggles.all().size(), is(3));
        assertThat(toggles.all().get(0), is(new FeatureToggle("key1", "NEW_desc1_WITH_NO_change_to_value", true).withValueChanged(false)));
        assertThat(toggles.all().get(1), is(new FeatureToggle("key2", "NEW_desc2_WITH_CHANGE_TO_VALUE", false).withValueChanged(true)));
        assertThat(toggles.all().get(2), is(new FeatureToggle("key3", "desc3", true).withValueChanged(false)));
    }
}
