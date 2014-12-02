package com.thoughtworks.go.server.service.support.toggle;

import com.thoughtworks.go.server.domain.support.toggle.FeatureToggle;
import com.thoughtworks.go.server.domain.support.toggle.FeatureToggles;
import com.thoughtworks.go.util.ListUtil;
import com.thoughtworks.go.util.SystemEnvironment;
import com.thoughtworks.go.util.TestFileUtil;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FeatureToggleRepositoryTest {
    @Mock
    private SystemEnvironment environment;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        TestFileUtil.cleanTempFiles();
    }

    @Test
    public void shouldReadFeatureTogglesFromAvailableTogglesFile() throws Exception {
        FeatureToggle featureToggle1 = new FeatureToggle("key1", "desc1", true);
        FeatureToggle featureToggle2 = new FeatureToggle("key2", "desc2", false);

        setupAvailableToggles(featureToggle1, featureToggle2);

        FeatureToggleRepository repository = new FeatureToggleRepository(environment);

        assertThat(repository.availableToggles(), is(new FeatureToggles(featureToggle1, featureToggle2)));
    }

    @Test
    public void shouldNotFailWhenSpecifiedAvailableTogglesFileIsNotFound() throws Exception {
        setupAvailableToggleFileAs(new File("a-non-existent-file"));

        FeatureToggleRepository repository = new FeatureToggleRepository(environment);

        assertThat(repository.availableToggles(), is(new FeatureToggles()));
    }

    @Test
    public void shouldNotFailWhenContentOfAvailableTogglesFileIsInvalid() throws Exception {
        File toggleFile = TestFileUtil.createTempFile("available.toggle.test");
        FileUtils.writeStringToFile(toggleFile, "SOME-INVALID-CONTENT");
        setupAvailableToggleFileAs(toggleFile);

        FeatureToggleRepository repository = new FeatureToggleRepository(environment);

        assertThat(repository.availableToggles(), is(new FeatureToggles()));
    }

    @Test
    public void shouldReadFeatureTogglesFromUsersTogglesFile() throws Exception {
        FeatureToggle featureToggle1 = new FeatureToggle("key1", "desc1", true);
        FeatureToggle featureToggle2 = new FeatureToggle("key2", "desc2", false);

        setupUserToggles(featureToggle1, featureToggle2);

        FeatureToggleRepository repository = new FeatureToggleRepository(environment);

        assertThat(repository.userToggles(), is(new FeatureToggles(featureToggle1, featureToggle2)));
    }

    @Test
    public void shouldNotFailWhenSpecifiedUserTogglesFileIsNotFound() throws Exception {
        setupUserToggleFileAs(new File("a-non-existent-file"));

        FeatureToggleRepository repository = new FeatureToggleRepository(environment);

        assertThat(repository.userToggles(), is(new FeatureToggles()));
    }

    @Test
    public void shouldNotFailWhenContentOfUserTogglesFileIsInvalid() throws Exception {
        File toggleFile = TestFileUtil.createTempFile("available.toggle.test");
        FileUtils.writeStringToFile(toggleFile, "SOME-INVALID-CONTENT");
        setupUserToggleFileAs(toggleFile);

        FeatureToggleRepository repository = new FeatureToggleRepository(environment);

        assertThat(repository.userToggles(), is(new FeatureToggles()));
    }

    private void setupAvailableToggleFileAs(File file) {
        when(environment.get(SystemEnvironment.AVAILABLE_FEATURE_TOGGLES_FILE_PATH)).thenReturn(file.getAbsolutePath());
    }

    private void setupUserToggleFileAs(File file) {
        when(environment.configDir()).thenReturn(file.getParentFile());
        when(environment.get(SystemEnvironment.USER_FEATURE_TOGGLES_FILE_PATH_RELATIVE_TO_CONFIG_DIR)).thenReturn(file.getName());
    }

    private File setupAvailableToggles(FeatureToggle... toggles) throws Exception {
        File toggleFile = TestFileUtil.createTempFile("available.toggle.test");
        setupAvailableToggleFileAs(toggleFile);
        writeToggles(toggleFile, toggles);
        return toggleFile;
    }

    private File setupUserToggles(FeatureToggle... toggles) throws Exception {
        File toggleFile = TestFileUtil.createTempFile("user.toggle.test");
        setupUserToggleFileAs(toggleFile);
        writeToggles(toggleFile, toggles);
        return toggleFile;
    }

    /* Write by hand to remove unnecessary coupling to actual write. */
    private void writeToggles(File toggleFile, FeatureToggle[] toggles) throws IOException {
        List<String> jsonContentForEachToggle = new ArrayList<String>();
        for (FeatureToggle toggle : toggles) {
            jsonContentForEachToggle.add(MessageFormat.format(
                    "'{'\"key\": \"{0}\", \"description\": \"{1}\", \"value\": {2}'}'",
                    toggle.key(), toggle.description(), String.valueOf(toggle.isOn())));
        }

        String jsonContent = "{ \"version\": \"1\", \"toggles\": [" + ListUtil.join(jsonContentForEachToggle, ",").trim() + "]}";
        FileUtils.writeStringToFile(toggleFile, jsonContent);
    }
}