package org.bouncycastle.asn1.x509;


import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.math.BigInteger;

/**
 * Class for containing a restriction object subtrees in NameConstraints. See
 * RFC 3280.
 * 
 * <pre>
 *       
 *       GeneralSubtree ::= SEQUENCE 
 *       {
 *         base                    GeneralName,
 *         minimum         [0]     BaseDistance DEFAULT 0,
 *         maximum         [1]     BaseDistance OPTIONAL 
 *       }
 * </pre>
 * 
 * @see org.bouncycastle.asn1.x509.NameConstraints
 * 
 */
public class GeneralSubtree 
    extends ASN1Encodable 
{
    private static final BigInteger ZERO = BigInteger.valueOf(0);

    private GeneralName base;

    private DERInteger minimum;

    private DERInteger maximum;

    public GeneralSubtree(
        ASN1Sequence seq) 
    {
        base = GeneralName.getInstance(seq.getObjectAt(0));

        switch (seq.size()) 
        {
        case 1:
            break;
        case 2:
            ASN1TaggedObject o = ASN1TaggedObject.getInstance(seq.getObjectAt(1));
            switch (o.getTagNo()) 
            {
            case 0:
                minimum = DERInteger.getInstance(o, false);
                break;
            case 1:
                maximum = DERInteger.getInstance(o, false);
                break;
            default:
                throw new IllegalArgumentException("Bad tag number: "
                        + o.getTagNo());
            }
            break;
        case 3:
            minimum = DERInteger.getInstance(ASN1TaggedObject.getInstance(seq.getObjectAt(1)));
            maximum = DERInteger.getInstance(ASN1TaggedObject.getInstance(seq.getObjectAt(2)));
            break;
        default:
            throw new IllegalArgumentException("Bad sequence size: "
                    + seq.size());
        }
    }

    /**
     * Constructor from a given details.
     * 
     * According RFC 3280, the minimum and maximum fields are not used with any
     * name forms, thus minimum MUST be zero, and maximum MUST be absent.
     * <p>
     * If minimum is <code>null</code>, zero is assumed, if
     * maximum is <code>null</code>, maximum is absent.
     * 
     * @param base
     *            A restriction.
     * @param minimum
     *            Minimum
     * 
     * @param maximum
     *            Maximum
     */
    public GeneralSubtree(
        GeneralName base,
        BigInteger minimum,
        BigInteger maximum)
    {
        this.base = base;
        if (maximum != null)
        {
            this.maximum = new DERInteger(maximum);
        }
        if (minimum == null)
        {
            this.minimum = null;
        }
        else
        {
            this.minimum = new DERInteger(minimum);
        }
    }

    public static GeneralSubtree getInstance(
        ASN1TaggedObject o,
        boolean explicit)
    {
        return new GeneralSubtree(ASN1Sequence.getInstance(o, explicit));
    }

    public static GeneralSubtree getInstance(
        Object obj)
    {
        if (obj == null)
        {
            return null;
        }

        if (obj instanceof GeneralSubtree)
        {
            return (GeneralSubtree) obj;
        }

        return new GeneralSubtree(ASN1Sequence.getInstance(obj));
    }

    public GeneralName getBase()
    {
        return base;
    }

    public BigInteger getMinimum()
    {
        if (minimum == null)
        {
            return ZERO;
        }

        return minimum.getValue();
    }

    public BigInteger getMaximum()
    {
        if (maximum == null)
        {
            return null;
        }

        return maximum.getValue();
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     * 
     * Returns:
     * 
     * <pre>
     *       GeneralSubtree ::= SEQUENCE 
     *       {
     *         base                    GeneralName,
     *         minimum         [0]     BaseDistance DEFAULT 0,
     *         maximum         [1]     BaseDistance OPTIONAL 
     *       }
     * </pre>
     * 
     * @return a DERObject
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(base);

        if (minimum != null && !minimum.getValue().equals(ZERO))
        {
            v.add(new DERTaggedObject(false, 0, minimum));
        }

        if (maximum != null)
        {
            v.add(new DERTaggedObject(false, 1, maximum));
        }

        return new DERSequence(v);
    }
}
