package io.vertigo.social.services.handle;

import java.util.List;

import io.vertigo.core.component.Component;
import io.vertigo.dynamo.domain.model.UID;

public interface HandleServices extends Component {

	List<Handle> searchHandles(final String handlePrefix);

	List<String> getHandlePrefixes();

	Handle getHandleByUid(UID uid);

	Handle getHandleByCode(String code);

	void reindexAll();

}
