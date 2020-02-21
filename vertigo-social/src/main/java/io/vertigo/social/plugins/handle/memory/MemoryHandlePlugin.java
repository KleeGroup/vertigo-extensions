package io.vertigo.social.plugins.handle.memory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.social.impl.handle.HandlePlugin;
import io.vertigo.social.services.handle.Handle;

public class MemoryHandlePlugin implements HandlePlugin {

	private final Map<UID, Handle> handlesPerUid = new HashMap<>();
	private final Map<String, Handle> handlesPerCode = new HashMap<>();

	/** {@inheritDoc} */
	@Override
	public void add(final List<Handle> handles) {
		handles.stream().forEach(handle -> {
			handlesPerUid.put(handle.getUid(), handle);
			handlesPerCode.put(handle.getCode(), handle);
		});
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final List<UID> uids) {
		uids.stream().forEach(uid -> {
			final Handle handle = handlesPerUid.remove(uid);
			if (handle != null) {
				handlesPerCode.remove(handle.getCode());
			}
		});
	}

	/** {@inheritDoc} */
	@Override
	public List<Handle> search(final String prefix) {
		return handlesPerUid.values().stream()
				.filter(handle -> handle.getCode().startsWith(prefix))
				.limit(10)
				.collect(Collectors.toList());
	}

	/** {@inheritDoc} */
	@Override
	public Handle getByCode(final String handleCode) {
		return handlesPerCode.get(handleCode);
	}

	/** {@inheritDoc} */
	@Override
	public Handle getByUid(final UID uid) {
		return handlesPerUid.get(uid);
	}

	/** {@inheritDoc} */
	@Override
	public void removeAll() {
		handlesPerUid.clear();
		handlesPerCode.clear();
	}

}
