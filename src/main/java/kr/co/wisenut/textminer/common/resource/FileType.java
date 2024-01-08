package kr.co.wisenut.textminer.common.resource;

import kr.co.wisenut.textminer.common.resource.StorageResourceType;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * supported file extension
 * <ul>
 *     <li>CSV : *.csv</li>
 *     <li>JSON : *.json</li>
 *     <li>SCD : *.scd</li>
 *     <li>TEXT : *.txt</li>
 * </ul>
 */
@Getter
public enum FileType {

    /**
     * csv : comma separated file
     */
    CSV("csv", StorageResourceType.COLLECTION, StorageResourceType.DICTIONARY),
    /**
     * json
     */
    JSON("json", StorageResourceType.COLLECTION, StorageResourceType.DICTIONARY),
    /**
     * scd: wisenut oriented document format
     */
    SCD("scd", StorageResourceType.COLLECTION, StorageResourceType.DICTIONARY),

    /**
     * plain text
     */
    TEXT("txt", StorageResourceType.DICTIONARY);

    /**
     * supported StorageResourceType set
     */
    private final Set<StorageResourceType> resourceTypes;

    /**
     * file extension
     */
    private final String extension;

    FileType(String extension, StorageResourceType... type) {
        this.extension = extension;

        Set<StorageResourceType> set = new HashSet();
        Collections.addAll(set, type);

        this.resourceTypes = Collections.unmodifiableSet(set);
    }

    public static FileType[] values(StorageResourceType resourceType) {
        return Arrays.stream(FileType.values())
                .filter(fileType -> fileType.getResourceTypes().contains(resourceType))
                .collect(Collectors.toList()).toArray(new FileType[0]);
    }

    public boolean isSupport(StorageResourceType type) {
        return resourceTypes.contains(type);
    }
}
