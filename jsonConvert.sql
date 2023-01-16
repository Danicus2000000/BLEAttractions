set @jsonOffline=(SELECT CONCAT('{"bledevices":',
JSON_ARRAYAGG(JSON_OBJECT('idBleDevice', idBleDevice, 'BleDeviceName', BleDeviceName,'BleDeviceOrganisation',BleDeviceOrganisation,'BleDeviceTransmitSignal',BleDeviceTransmitSignal,'BleDeviceUrlToPointTo',BleDeviceUrlToPointTo,'BleDeviceQueueTime',BleDeviceQueueTime,'BleDeviceTitle',BleDeviceTitle))
,',"organisations":') from bledevices);
set @jsonOffline=(select concat(@jsonOffline,JSON_ARRAYAGG(JSON_OBJECT('idOrganisation', idOrganisation, 'OrganisationName', OrganisationName,'OrganisationLogoUrl',OrganisationLogoUrl,'OrganisationColourScheme',OrganisationColourScheme)),'}') from organisations);
select JSON_PRETTY(@jsonOffline) as offlineData;