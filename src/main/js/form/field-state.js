import Enum from "../util/enum"

/**
 * Field state enum.
 *
 * @type
 */
const FieldState = new Enum({
    NORMAL: true,
    DISABLED: true,
    READ_ONLY: true
});
export default FieldState;
