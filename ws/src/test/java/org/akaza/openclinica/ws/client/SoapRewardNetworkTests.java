package org.akaza.openclinica.ws.client;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Locale;

public class SoapRewardNetworkTests extends AbstractDependencyInjectionSpringContextTests {

    private SoapCreateSubject rewardNetwork;

    public void setRewardNetwork(SoapCreateSubject rewardNetwork) {
        this.rewardNetwork = rewardNetwork;
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] { "classpath*:org/akaza/openclinica/ws/client/client-config.xml" };
    }

    private void initializeLocale() {
        String locale = "en_US";
        ResourceBundleProvider.updateLocale(new Locale(locale));
    }

    public void testRewardForDining() {
        initializeLocale();
        // create a new dining of 100.00 charged to credit card '1234123412341234' by merchant '123457890' as test input
        StudyBean studyBean = new StudyBean();
        studyBean.setIdentifier("default-study");

        SubjectBean subjectBean = new SubjectBean();
        subjectBean.setUniqueIdentifier("krikor");

        // Dining dining = Dining.createDining("100.00", "1234123412341234", "1234567890");

        // call the 'rewardNetwork' to test its rewardAccountFor(Dining) method
        // RewardConfirmation confirmation = rewardNetwork.rewardAccountFor(dining);
        String result = rewardNetwork.createSubject(subjectBean, studyBean, null, null);

        System.out.println(result);
        assertNotNull(result);
    }

}
