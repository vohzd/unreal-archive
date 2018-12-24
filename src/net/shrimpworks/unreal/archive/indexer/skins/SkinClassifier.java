package net.shrimpworks.unreal.archive.indexer.skins;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;

import net.shrimpworks.unreal.archive.indexer.Classifier;
import net.shrimpworks.unreal.archive.indexer.Incoming;
import net.shrimpworks.unreal.archive.indexer.IndexUtils;
import net.shrimpworks.unreal.packages.IntFile;

/**
 * A skin should contain:
 * <p>
 * - At least one .utx file
 * - One .int file
 * <p>
 * The .int file should contain a [public] section, and an entry which follows the format:
 * <pre>
 * [public]
 * Object=(Name=ModelReference_Something.tex1,Class=Texture,Description="Character")
 * </pre>
 * <p>
 * If there's a .u file, or more .int files (with other contents), it's probably a model.
 */
public class SkinClassifier implements Classifier {

	@Override
	public boolean classify(Incoming incoming) {
		Set<Incoming.IncomingFile> intFiles = incoming.files(Incoming.FileType.INT);
		Set<Incoming.IncomingFile> playerFiles = incoming.files(Incoming.FileType.PLAYER);
		Set<Incoming.IncomingFile> codeFiles = incoming.files(Incoming.FileType.CODE);

		// presence of a .u package probably indicates a model
		if (!codeFiles.isEmpty()) return false;

		// there should be no maps in a skin... otherwise this may be a mod
		if (!incoming.files(Incoming.FileType.MAP).isEmpty()) return false;

		// more often than not multiple ints probably indicates a model
		if (intFiles.size() != 1 && playerFiles.size() != 1) return false;

		if (incoming.files(Incoming.FileType.TEXTURE).isEmpty()) return false;

		if (!intFiles.isEmpty()) return utSkin(incoming, intFiles);
		else if (!playerFiles.isEmpty()) return ut2004Skin(incoming, playerFiles);

		return false;
	}

	private boolean utSkin(Incoming incoming, Set<Incoming.IncomingFile> intFiles) {
		boolean[] seemsToBeASkin = new boolean[] { false };

		// search int files for objects describing a skin
		IndexUtils.readIntFiles(incoming, intFiles)
				  .filter(Objects::nonNull)
				  .forEach(intFile -> {
					  IntFile.Section section = intFile.section("public");
					  if (section == null) return;

					  IntFile.ListValue objects = section.asList("Object");
					  for (IntFile.Value value : objects.values) {
						  if (value instanceof IntFile.MapValue
							  && ((IntFile.MapValue)value).value.containsKey("Name")
							  && ((IntFile.MapValue)value).value.containsKey("Class")
							  && ((IntFile.MapValue)value).value.containsKey("Description")
							  && ((IntFile.MapValue)value).value.get("Class").equalsIgnoreCase("Texture")) {

							  Matcher m = Skin.NAME_MATCH.matcher(((IntFile.MapValue)value).value.get("Name"));
							  if (m.matches()) {
								  seemsToBeASkin[0] = true;
								  return;
							  }
						  }
					  }

				  });

		return seemsToBeASkin[0];
	}

	private boolean ut2004Skin(Incoming incoming, Set<Incoming.IncomingFile> playerFiles) {
		// indicates a model - presence of a player file indicates a plain skin
		return incoming.files(Incoming.FileType.ANIMATION).isEmpty();
	}
}
