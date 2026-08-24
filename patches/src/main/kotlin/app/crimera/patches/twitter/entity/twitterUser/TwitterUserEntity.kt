/*
 * Copyright (C) 2026 piko <https://github.com/crimera/piko>
 *
 * See the included NOTICE file for GPLv3 §7(b) terms that apply to this code.
 */

package app.crimera.patches.twitter.entity.twitterUser

import app.crimera.utils.changeFirstString
import app.crimera.utils.fieldExtractor
import app.morphe.patcher.Match
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode

val twitterUserEntity =
    bytecodePatch(
        description = "For Twitter user entity reflection",
    ) {
        execute {
            // Chỉ lấy tương ứng 4 extension match với 4 string trong Fingerprints.kt đã sửa
            val fingerprintList =
                listOf(
                    GetFastFollowersCountExtension,
                    GetStatusCountExtension,
                    GetMediaCountExtension,
                    GetLikesCountExtension,
                )

            TwitterUserToStringFingerprint.apply {
                val stringMatches = stringMatches
                method.apply {
                    STRING_LIST.forEachIndexed { strListIndex, str ->
                        val match = stringMatches.firstOrNull { it.string == str } ?: return@forEachIndexed
                        val strIndex = match.index
                        
                        // Quét lấy đúng lệnh iget/iget-wide gần nhất ngay sau chuỗi thay vì +2 cứng nhắc
                        val fieldInstructionIndex = indexOfFirstInstruction(strIndex) {
                            it.opcode.name.startsWith("iget")
                        }

                        if (fieldInstructionIndex != -1) {
                            val valueInstruction = getInstruction(fieldInstructionIndex)
                            val fieldName = valueInstruction.fieldExtractor().name
                            fingerprintList[strListIndex].changeFirstString(fieldName)
                        }
                    }
                }
            }
        }
    }
