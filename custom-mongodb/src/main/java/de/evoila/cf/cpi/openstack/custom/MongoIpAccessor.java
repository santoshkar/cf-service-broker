/**
 * 
 */
package de.evoila.cf.cpi.openstack.custom;

import java.util.List;
import java.util.Map;

import org.openstack4j.model.heat.Stack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.evoila.cf.broker.model.ServerAddress;
import de.evoila.cf.cpi.openstack.OpenstackServiceFactory;
import de.evoila.cf.cpi.openstack.fluent.HeatFluent;
import jersey.repackaged.com.google.common.collect.Lists;

/**
 * @author Christian Brinker, evoila.
 *
 */
@Service
public class MongoIpAccessor extends CustomIpAccessor {

	private HeatFluent heatFluent;

	@Override
	public List<ServerAddress> getIpAddresses(String instanceId) {
		Stack stack = heatFluent.get(OpenstackServiceFactory.uniqueName(instanceId));
		List<Map<String, Object>> outputs = stack.getOutputs();
		List<ServerAddress> serverAddresses = Lists.newArrayList();
		for (Map<String, Object> output : outputs) {
			Object outputKey = output.get("output_key");
			if (outputKey != null && outputKey instanceof String) {
				String key = (String) outputKey;
				if (key.equals("sec_ips")) {
					List<String> outputValue = (List<String>) output.get("output_value");

					for (int i = 0; i < outputValue.size(); i++) {
						serverAddresses
								.add(new ServerAddress("sec_ips" + "#" + Integer.toString(i), outputValue.get(i)));
					}
				} else if (key.equals("prim_ip")) {
					String outputValue = (String) output.get("output_value");

					serverAddresses.add(new ServerAddress("prim_ip", outputValue));
				}
			}
		}

		return serverAddresses;
	}

	@Autowired
	public void setHeatFluent(HeatFluent heatFluent) {
		this.heatFluent = heatFluent;
	}
}
