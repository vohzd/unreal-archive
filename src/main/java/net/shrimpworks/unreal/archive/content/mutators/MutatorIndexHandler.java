package net.shrimpworks.unreal.archive.content.mutators;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import net.shrimpworks.unreal.archive.content.Content;
import net.shrimpworks.unreal.archive.content.Incoming;
import net.shrimpworks.unreal.archive.content.IndexHandler;
import net.shrimpworks.unreal.archive.content.IndexLog;
import net.shrimpworks.unreal.archive.content.IndexResult;
import net.shrimpworks.unreal.archive.content.IndexUtils;
import net.shrimpworks.unreal.archive.content.NameDescription;
import net.shrimpworks.unreal.packages.IntFile;

import static net.shrimpworks.unreal.archive.content.mutators.Mutator.*;

public class MutatorIndexHandler implements IndexHandler<Mutator> {

	public static class ModelIndexHandlerFactory implements IndexHandlerFactory<Mutator> {

		@Override
		public IndexHandler<Mutator> get() {
			return new MutatorIndexHandler();
		}
	}

	@Override
	public void index(Incoming incoming, Content current, Consumer<IndexResult<Mutator>> completed) {
		Mutator m = (Mutator)current;
		IndexLog log = incoming.log;

		Set<IndexResult.NewAttachment> attachments = new HashSet<>();

		m.name = IndexUtils.friendlyName(m.name);

		Set<Incoming.IncomingFile> uclFiles = incoming.files(Incoming.FileType.UCL);
		Set<Incoming.IncomingFile> intFiles = incoming.files(Incoming.FileType.INT);
		Set<Incoming.IncomingFile> iniFiles = incoming.files(Incoming.FileType.INI);

		if (!uclFiles.isEmpty()) {
			// find mutator information via .ucl files (mutator names, descriptions, weapons and vehicles)
			readUclFiles(incoming, m, uclFiles, intFiles);
		} else if (!intFiles.isEmpty()) {
			// find mutator information via .int files (weapon names, menus, keybindings)
			readIntFiles(incoming, m, intFiles);
		}

		// try to find UT3 mutators - there is some overlap in that other games may also include int and ini files
		if (m.mutators.isEmpty() && !iniFiles.isEmpty()) {
			readIniFiles(incoming, m, iniFiles);
		}

		// if there's only one mutator, rename package to that
		if (m.mutators.size() == 1) {
			m.name = m.mutators.get(0).name;
			m.description = m.mutators.get(0).description;
		}

		m.game = IndexUtils.game(incoming).name;

		m.author = IndexUtils.findAuthor(incoming, true);

		try {
			// see if there are any images the author may have included in the package
			List<BufferedImage> images = IndexUtils.findImageFiles(incoming);
			IndexUtils.saveImages(IndexUtils.SHOT_NAME, m, images, attachments);
		} catch (IOException e) {
			log.log(IndexLog.EntryType.CONTINUE, "Failed to save images", e);
		}

		completed.accept(new IndexResult<>(m, attachments));
	}

	private void readUclFiles(
		Incoming incoming, Mutator mutator, Set<Incoming.IncomingFile> uclFiles, Set<Incoming.IncomingFile> intFiles) {
		// search ucl files for objects describing a mutator and related things
		IndexUtils.readIntFiles(incoming, uclFiles, true)
				  .filter(Objects::nonNull)
				  .forEach(intFile -> {
					  IntFile.Section section = intFile.section("root");
					  if (section == null) return;

					  // read mutators
					  IntFile.ListValue mutators = section.asList("Mutator");
					  for (IntFile.Value value : mutators.values) {
						  if (!(value instanceof IntFile.MapValue)) continue;
						  IntFile.MapValue mapVal = (IntFile.MapValue)value;
						  if (mapVal.containsKey("FallbackName")) {
							  mutator.mutators.add(new NameDescription(mapVal.get("FallbackName"),
																	   mapVal.getOrDefault("FallbackDesc", "")));
						  }
					  }

					  // read weapons
					  IntFile.ListValue weapons = section.asList("Weapon");
					  for (IntFile.Value value : weapons.values) {
						  if (!(value instanceof IntFile.MapValue)) continue;
						  IntFile.MapValue mapVal = (IntFile.MapValue)value;
						  if (mapVal.containsKey("FallbackName")) {
							  mutator.weapons.add(new NameDescription(mapVal.get("FallbackName"),
																	  mapVal.getOrDefault("FallbackDesc", "")));
						  }
					  }

					  // read vehicles
					  IntFile.ListValue vehicles = section.asList("Vehicle");
					  for (IntFile.Value value : vehicles.values) {
						  if (!(value instanceof IntFile.MapValue)) continue;
						  IntFile.MapValue mapVal = (IntFile.MapValue)value;
						  if (mapVal.containsKey("FallbackName")) {
							  mutator.vehicles.add(new NameDescription(mapVal.get("FallbackName"),
																	   mapVal.getOrDefault("FallbackDesc", "")));
						  }
					  }
				  });

		// find other information not exposed by ucl files
		IndexUtils.readIntFiles(incoming, intFiles)
				  .filter(Objects::nonNull)
				  .forEach(intFile -> {
					  IntFile.Section section = intFile.section("public");
					  if (section == null) return;

					  IntFile.ListValue objects = section.asList("Object");
					  for (IntFile.Value value : objects.values) {
						  if (!(value instanceof IntFile.MapValue)) continue;
						  IntFile.MapValue mapVal = (IntFile.MapValue)value;

						  if (mapVal.containsKey("MetaClass") && Mutator.UT2_KEYBINDINGS_CLASS.equalsIgnoreCase(mapVal.get("MetaClass"))) {
							  mutator.hasKeybinds = true;
						  }
					  }
				  });
	}

	private void readIntFiles(Incoming incoming, Mutator mutator, Set<Incoming.IncomingFile> intFiles) {
		// search int files for objects describing a mutator and related things
		IndexUtils.readIntFiles(incoming, intFiles)
				  .filter(Objects::nonNull)
				  .forEach(intFile -> {
					  IntFile.Section section = intFile.section("public");
					  if (section == null) return;

					  IntFile.ListValue objects = section.asList("Object");
					  for (IntFile.Value value : objects.values) {
						  if (!(value instanceof IntFile.MapValue)) continue;
						  IntFile.MapValue mapVal = (IntFile.MapValue)value;

						  if (!mapVal.containsKey("MetaClass")) continue;

						  if (Mutator.UT_MUTATOR_CLASS.equalsIgnoreCase(mapVal.get("MetaClass"))) {
							  mutator.mutators.add(new NameDescription(mapVal.get("Description")));
						  } else if (Mutator.UT_WEAPON_CLASS.equalsIgnoreCase(mapVal.get("MetaClass"))) {
							  mutator.weapons.add(new NameDescription(mapVal.get("Description")));
						  } else if (Mutator.UT_KEYBINDINGS_CLASS.equalsIgnoreCase(mapVal.get("MetaClass"))) {
							  mutator.hasKeybinds = true;
						  } else if (Mutator.UT_MENU_CLASS.equalsIgnoreCase(mapVal.get("MetaClass"))) {
							  mutator.hasConfigMenu = true;
						  }
					  }
				  });
	}

	private void readIniFiles(Incoming incoming, Mutator mutator, Set<Incoming.IncomingFile> iniFiles) {
		// search int files for objects describing a mutator and related things
		IndexUtils.readIntFiles(incoming, iniFiles)
				  .filter(Objects::nonNull)
				  .forEach(iniFile -> {
					  iniFile.sections().forEach(name -> {
						  IntFile.Section section = iniFile.section(name);
						  if (name.toLowerCase().endsWith(UT3_MUTATOR_SECTION.toLowerCase())) {
							  // add mutator
							  mutator.mutators.add(sectionToNameDesc(section, mutator));
						  } else if (name.toLowerCase().endsWith(UT3_WEAPON_SECTION.toLowerCase())) {
							  // add weapon
							  mutator.weapons.add(sectionToNameDesc(section, mutator));
						  } else if (name.toLowerCase().endsWith(UT3_VEHICLE_SECTION.toLowerCase())) {
							  // add vehicle
							  mutator.vehicles.add(sectionToNameDesc(section, mutator));
						  }

						  // check for custom configuration things
						  if (section.value("UIConfigScene") != null && !section.value("UIConfigScene").toString().isBlank()) {
							  mutator.hasConfigMenu = true;
						  }
					  });
				  });
	}

	private NameDescription sectionToNameDesc(IntFile.Section section, Mutator mutator) {
		IntFile.Value friendlyName = section.value("FriendlyName");
		IntFile.Value description = section.value("Description");

		String nameString = mutator.name();
		String descString = "";
		if (friendlyName != null) nameString = friendlyName.toString();
		if (description != null) descString = description.toString();

		return new NameDescription(nameString, descString);
	}

}
