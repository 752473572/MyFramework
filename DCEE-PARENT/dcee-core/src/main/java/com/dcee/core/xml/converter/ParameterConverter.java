package com.dcee.core.xml.converter;

import java.util.Iterator;
import java.util.Map;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class ParameterConverter extends MapConverter {
	public ParameterConverter(Mapper mapper) {
		super(mapper);
	}

	@SuppressWarnings("rawtypes")
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		Map map = (Map) source;
		for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			ExtendedHierarchicalStreamWriterHelper.startNode(writer, "parameter", Map.Entry.class);

			writer.addAttribute("key", entry.getKey().toString());
			writer.setValue(entry.getValue().toString());
			writer.endNode();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void populateMap(HierarchicalStreamReader reader, UnmarshallingContext context, Map map) {
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			Object key = reader.getAttribute("key");
			Object value = reader.getValue();
			map.put(key, value);
			reader.moveUp();
		}
	}
}
