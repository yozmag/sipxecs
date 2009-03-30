package org.sipfoundry.sipxconfig.service;

import org.sipfoundry.sipxconfig.TestHelper;
import org.sipfoundry.sipxconfig.admin.commserver.Location;
import org.sipfoundry.sipxconfig.admin.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.setting.Setting;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class SipxCallResolverConfigurationTest extends SipxServiceTestBase {

    public void testWrite() throws Exception {
        Location primary = new Location();
        primary.setPrimary(true);
        primary.setFqdn("localhost");
        primary.setAddress("192.168.1.1");
        primary.setName("primary");
        Location secondary = new Location();
        secondary.setPrimary(false);
        secondary.setFqdn("remote.exampl.com");
        secondary.setAddress("192.168.1.2");
        secondary.setName("remote");

        LocationsManager locationManager = createMock(LocationsManager.class);

        locationManager.getPrimaryLocation();
        expectLastCall().andReturn(primary).anyTimes();
        locationManager.getLocations();
        expectLastCall().andReturn(new Location[] { primary, secondary }).anyTimes();

        SipxCallResolverService callResolverService = new SipxCallResolverService();
        initCommonAttributes(callResolverService);
        Setting settings = TestHelper.loadSettings("sipxcallresolver/sipxcallresolver.xml");
        callResolverService.setSettings(settings);
        callResolverService.setLocationManager(locationManager);

        Setting callresolverSettings = callResolverService.getSettings().getSetting("callresolver");
        callresolverSettings.getSetting("SIP_CALLRESOLVER_PURGE").setValue("DISABLE");
        callresolverSettings.getSetting("SIP_CALLRESOLVER_PURGE_AGE_CDR").setValue("40");
        callresolverSettings.getSetting("SIP_CALLRESOLVER_PURGE_AGE_CSE").setValue("10");
        callresolverSettings.getSetting("SIP_CALLRESOLVER_LOG_LEVEL").setValue("CRIT");

        callResolverService.setAgentPort(8090);

        SipxServiceManager sipxServiceManager = createMock(SipxServiceManager.class);
        sipxServiceManager.getServiceByBeanId(SipxCallResolverService.BEAN_ID);
        expectLastCall().andReturn(callResolverService).atLeastOnce();
        replay(locationManager, sipxServiceManager);

        SipxCallResolverConfiguration out = new SipxCallResolverConfiguration();
        out.setSipxServiceManager(sipxServiceManager);
        out.setLocationsManager(locationManager);
        out.setTemplate("sipxcallresolver/callresolver-config.vm");

        assertCorrectFileGeneration(out, "expected-callresolver-config");

        verify(locationManager, sipxServiceManager);
    }


}
